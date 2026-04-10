package com.zafer.otelmaris.view

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.google.firebase.database.*
import com.zafer.otelmaris.R
import com.zafer.otelmaris.adapter.RezervasyonAdapter
import com.zafer.otelmaris.model.Rezervasyon
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.iterator

class RaporActivity : AppCompatActivity(), RezervasyonIptalListener {

    private lateinit var database: DatabaseReference
    private lateinit var pieChart: PieChart
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RezervasyonAdapter
    private val tumRezervasyonlar = ArrayList<Rezervasyon>()
    private var filtrelenmisListe = ArrayList<Rezervasyon>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rapor)

        database = FirebaseDatabase.getInstance().getReference("Rezervasyonlar")
        pieChart = findViewById(R.id.pieChart)
        recyclerView = findViewById(R.id.rvRezervasyonlar)

        // NestedScrollView içinde olduğu için kaydırmayı kapatıyoruz
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.isNestedScrollingEnabled = false

        adapter = RezervasyonAdapter(this, filtrelenmisListe, this)
        recyclerView.adapter = adapter

        val btnGunluk = findViewById<Button>(R.id.btnGunluk)
        val btnHaftalik = findViewById<Button>(R.id.btnHaftalik)
        val btnAylik = findViewById<Button>(R.id.btnAylik)

        verileriGetir()

        btnGunluk.setOnClickListener { filtrele("gun") }
        btnHaftalik.setOnClickListener { filtrele("hafta") }
        btnAylik.setOnClickListener { filtrele("ay") }
    }

    // --- SENKRONİZE SİLME VE GÜNCELLEME ---
    override fun onRezervasyonIptal(rezervasyon: Rezervasyon) {
        val rezId = rezervasyon.rezervasyonId
        val odaNo = rezervasyon.odaNo

        if (rezId != null && odaNo != null) {
            // 1. Rezervasyon kaydını siler
            database.child(rezId).removeValue().addOnSuccessListener {

                // 2. Odayı günceller (Müsait yerine 'uygun' yazacak)
                val odaRef = FirebaseDatabase.getInstance().getReference("Odalar").child(odaNo)

                val guncellemeMap = HashMap<String, Any>()
                guncellemeMap["durum"] = "uygun" // BURAYI DÜZELTTİK
                guncellemeMap["musaitMi"] = true

                odaRef.updateChildren(guncellemeMap).addOnSuccessListener {
                    Toast.makeText(this, "Rezervasyon silindi ve Oda #$odaNo 'uygun' yapıldı.", Toast.LENGTH_SHORT).show()
                }

            }.addOnFailureListener {
                Toast.makeText(this, "Hata: Silme işlemi başarısız.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Hata: Kayıt bilgileri eksik!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun verileriGetir() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                tumRezervasyonlar.clear()
                for (postSnapshot in snapshot.children) {
                    val rez = postSnapshot.getValue(Rezervasyon::class.java)

                    // Eğer kullanıcının iptal ettiği rezervasyonun durumu "iptal edildi" ise raporda gözükmez.
                    if (rez != null && rez.durum != "iptal edildi") {
                        if (rez.rezervasyonId == null) rez.rezervasyonId = postSnapshot.key
                        tumRezervasyonlar.add(rez)
                    }
                }

                filtrele("ay") // Açılışta aylık verileri getir
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "Veri çekme hatası: ${error.message}")
            }
        })
    }

    private fun filtrele(tip: String) {
        val takvim = Calendar.getInstance()
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val bugunStr = format.format(takvim.time)

        filtrelenmisListe.clear()
        var toplamGelir = 0.0
        val odaSayaclari = HashMap<String, Int>()

        for (rez in tumRezervasyonlar) {
            val rezTarihi = rez.cikisTarihi ?: "" // Bu sadece referans için, aşağıda parse edeceğiz
            var uygunMu = false

            when (tip) {
                // --- İŞTE BURAYI GÜNCELLEDİK ---
                "gun" -> {
                    try {
                        val bugunDate = format.parse(bugunStr)
                        // Giriş ve Çıkış tarihlerini alıyoruz
                        // NOT: Rezervasyon sınıfında 'girisTarihi' olduğunu varsayıyoruz
                      val girisDate = format.parse(rez.girisTarihi ?: "")
                      val cikisDate = format.parse(rez.cikisTarihi ?: "")

                      if (bugunDate != null && girisDate != null && cikisDate != null) {
                          // Mantık: Bugün tarihi, Giriş tarihinden büyük/eşit VE Çıkış tarihinden küçük/eşit mi?
                          // Yani müşteri şu an içeride mi?
                            if (bugunDate.compareTo(girisDate) >= 0 && bugunDate.compareTo(cikisDate) <= 0) {
                                uygunMu = true
                            }
                        }
                    } catch (e: Exception) {
                        // Tarih formatı hatası varsa bu kaydı atla
                    }
                }
                "hafta" -> {
                    val yediGunOnce = Calendar.getInstance()
                    yediGunOnce.add(Calendar.DAY_OF_YEAR, -7)
                    try {
                        val rezDate = format.parse(rezTarihi)
                        if (rezDate != null && rezDate.after(yediGunOnce.time)) uygunMu = true
                    } catch (e: Exception) {}
                }
                "ay" -> {
                    if (rezTarihi.length >= 7 && bugunStr.length >= 7) {
                        val buAy = bugunStr.substring(0, 7)
                        if (rezTarihi.startsWith(buAy)) uygunMu = true
                    }
                }
            }

            if (uygunMu) {
                filtrelenmisListe.add(rez)
                toplamGelir += rez.toplamFiyat ?: 0.0
                val odaNo = rez.odaNo ?: "Bilinmeyen"
                odaSayaclari[odaNo] = odaSayaclari.getOrDefault(odaNo, 0) + 1
            }
        }

        // UI Güncellemeleri
        findViewById<TextView>(R.id.tvToplamGelir).text = String.format(Locale.getDefault(), "%.2f ₺", toplamGelir)
        findViewById<TextView>(R.id.tvToplamRezervasyon).text = filtrelenmisListe.size.toString()

        adapter.listeyiGuncelle(filtrelenmisListe)
        grafigiCiz(odaSayaclari, tip)
    }

    private fun grafigiCiz(odaSayaclari: HashMap<String, Int>, baslik: String) {
        val entries = ArrayList<PieEntry>()
        for ((oda, sayi) in odaSayaclari) {
            entries.add(PieEntry(sayi.toFloat(), "Oda $oda"))
        }

        if (entries.isEmpty()) {
            pieChart.clear()
            pieChart.setNoDataText("Bu dönemde aktif veri bulunamadı")
            pieChart.invalidate()
            return
        }

        val dataSet = PieDataSet(entries, "")
        val marisColors = arrayListOf(
            Color.parseColor("#003366"), Color.parseColor("#00A5B5"),
            Color.parseColor("#FFD700"), Color.parseColor("#1A202C")
        )

        dataSet.apply {
            colors = marisColors
            valueTextSize = 14f
            valueTextColor = Color.WHITE
            sliceSpace = 3f
        }

        pieChart.apply {
            data = PieData(dataSet).apply { setValueFormatter(PercentFormatter(pieChart)) }
            setUsePercentValues(true)
            description.isEnabled = false
            centerText = "${baslik.uppercase()}\nODA DAĞILIMI"
            setCenterTextColor(Color.parseColor("#003366"))
            setCenterTextSize(14f)
            animateY(1000)
            invalidate()
        }
    }
}