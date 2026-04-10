package com.zafer.otelmaris.view

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.zafer.otelmaris.adapter.OdaAdapter
import com.zafer.otelmaris.databinding.ActivityAdminEkraniBinding
import com.zafer.otelmaris.databinding.ActivityOdaEkleBinding
import com.zafer.otelmaris.databinding.ActivityOdaListeleBinding
import com.zafer.otelmaris.model.Oda

class AdminEkrani : AppCompatActivity() {

    private lateinit var binding: ActivityAdminEkraniBinding
    private lateinit var database: DatabaseReference
    private var isDeleteMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAdminEkraniBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance().getReference("Odalar")

        binding.btnEkle.setOnClickListener { displayOdaEkleView() }

        binding.btnListele.setOnClickListener {
            isDeleteMode = false
            binding.btnSilModu.text = "Oda Silme Modu"
            displayOdaListView()
        }

        binding.btnSilModu.setOnClickListener {
            // Silme modunu aç/kapat
            isDeleteMode = !isDeleteMode
            binding.btnSilModu.text = if (isDeleteMode) "Silme Modu: KAPAT" else "Oda Silme Modu"
            displayOdaListView()
        }

        // Çıkış Yap Butonu
        binding.btnAdminCikisYap.setOnClickListener {
            cikisYap()
        }

        //Rapor
        binding.btnR.setOnClickListener {
            val yeniIntent = Intent(this@AdminEkrani, RaporActivity::class.java)
            startActivity(yeniIntent)
        }

        displayOdaListView()
    }


    // Çıkış Yapma Fonksiyonu
    private fun cikisYap() {
        Toast.makeText(this, "Yönetici Çıkışı Yapılıyor...", Toast.LENGTH_SHORT).show()
        // Ana giriş ekranına dönülüyor.
        val intent = Intent(this, GirisYap::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }


    private fun displayOdaEkleView() {
        val odaEkleBinding = ActivityOdaEkleBinding.inflate(LayoutInflater.from(this))

        binding.contentFrame.removeAllViews()
        binding.contentFrame.addView(odaEkleBinding.root)

        odaEkleBinding.btnOdaEkle.setOnClickListener {
            yeniOdaEkle(odaEkleBinding)
        }
    }

    private fun displayOdaListView() {
        val odaListeleBinding = ActivityOdaListeleBinding.inflate(LayoutInflater.from(this))

        binding.contentFrame.removeAllViews()
        binding.contentFrame.addView(odaListeleBinding.root)

        val rvOdaListesi: RecyclerView = odaListeleBinding.rvOdaListesi
        val tvSilModu = odaListeleBinding.tvSilModu

        rvOdaListesi.layoutManager = LinearLayoutManager(this)


        if (isDeleteMode) {
            tvSilModu.visibility = View.VISIBLE
            tvSilModu.text = "⚠️ SİLME MODU AÇIK! SİL yazısına tıklayarak silin."
        } else {
            tvSilModu.visibility = View.GONE
        }

        loadOdaListesi(rvOdaListesi)
    }

    // --- Veritabanı İşlemleri ---
    private fun yeniOdaEkle(odaEkleBinding: ActivityOdaEkleBinding) {
        // ... (Veri alma ve kontrol kısmı)
        val odaNo = odaEkleBinding.etOdaNo.text.toString().trim()
        val tip = odaEkleBinding.etOdaTip.text.toString().trim()
        val fiyatStr = odaEkleBinding.etFiyat.text.toString().trim()
        val fiyat = fiyatStr.replace(',', '.').toDoubleOrNull()

        val temizlikDurumu = if (odaEkleBinding.chkTemiz.isChecked) "Temiz" else "Kirli"
        val musaitMi = true

        if (odaNo.isEmpty() || tip.isEmpty() || fiyat == null || fiyat <= 0) {
            Toast.makeText(this, "Lütfen tüm oda bilgilerini eksiksiz ve fiyatı doğru girin.", Toast.LENGTH_LONG).show()
            return
        }

        // Oda numarası kontrolü
        database.child(odaNo).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                Toast.makeText(this, "❌ Hata: $odaNo Numaralı Oda Zaten Mevcut!", Toast.LENGTH_LONG).show()
            } else {
                val yeniOda = Oda(odaNo, tip, fiyat, temizlikDurumu, musaitMi)

                // Firebase'e kaydetme
                database.child(odaNo).setValue(yeniOda)
                    .addOnSuccessListener {
                        Toast.makeText(this, "✅ $odaNo Numaralı Oda Başarıyla Eklendi!", Toast.LENGTH_SHORT).show()
                        // Alanları temizle
                        odaEkleBinding.etOdaNo.text.clear()
                        odaEkleBinding.etOdaTip.text.clear()
                        odaEkleBinding.etFiyat.text.clear()
                        odaEkleBinding.chkTemiz.isChecked = true

                        displayOdaListView() // Listeyi otomatik yenile
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "❌ Oda eklenirken hata oluştu: ${it.message}", Toast.LENGTH_LONG).show()
                    }
            }
        }
    }

    private fun loadOdaListesi(recyclerView: RecyclerView) {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val odaList = arrayListOf<Oda>()
                if (snapshot.exists()) {
                    for (odaSnapshot in snapshot.children) {
                        val oda = odaSnapshot.getValue(Oda::class.java)
                        oda?.let {
                            odaList.add(it)
                        }
                    }
                }


                val odaAdapter = OdaAdapter(
                    context = this@AdminEkrani,
                    odaList = odaList,
                    isDeleteMode = isDeleteMode,       // Admin silme modu açık/kapalı
                    isPersonnelMode = false,
                    temizlikListener = null,
                    onClick = { oda ->

                        if (isDeleteMode) {
                            odaSil(oda.odaNo ?: "")
                        } else {
                            Toast.makeText(
                                this@AdminEkrani,
                                "Oda Detay: ${oda.odaNo}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                )
                recyclerView.adapter = odaAdapter
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@AdminEkrani, "Veri Çekme Hatası: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun odaSil(odaNo: String) {
        if (odaNo.isEmpty()) return

        // Firebase'den silme işlemi
        database.child(odaNo).removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "$odaNo Numaralı Oda Silindi.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Silme Başarısız: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}