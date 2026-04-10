package com.zafer.otelmaris.view

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.*
import com.zafer.otelmaris.adapter.OdaAdapter
import com.zafer.otelmaris.R
import com.zafer.otelmaris.adapter.RezervasyonAdapter
import com.zafer.otelmaris.databinding.ActivityKullaniciEkraniBinding
import com.zafer.otelmaris.databinding.DialogRezervasyonFormuBinding
import com.zafer.otelmaris.model.Oda
import com.zafer.otelmaris.model.Rezervasyon
import com.zafer.otelmaris.model.Users
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


enum class Screen {
    ROOM_LIST, RESERVATIONS, PROFILE
}

interface RezervasyonIptalListener {
    fun onRezervasyonIptal(rezervasyon: Rezervasyon)
}

class KullaniciEkrani : AppCompatActivity(), RezervasyonIptalListener {

    private lateinit var binding: ActivityKullaniciEkraniBinding
    private lateinit var databaseOdalar: DatabaseReference
    private lateinit var databaseRezervasyonlar: DatabaseReference
    private lateinit var databaseKullanicilar: DatabaseReference

    private var girisYapanKullaniciAdi: String = ""
    private var kullaniciAdSoyad: String = ""

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        binding = ActivityKullaniciEkraniBinding.inflate(layoutInflater)
        setContentView(binding.root)

        girisYapanKullaniciAdi = intent.getStringExtra("kullaniciAdi")?.trim() ?: "Bilinmeyen"

        databaseOdalar = FirebaseDatabase.getInstance().getReference("Odalar")
        databaseRezervasyonlar = FirebaseDatabase.getInstance().getReference("Rezervasyonlar")
        databaseKullanicilar = FirebaseDatabase.getInstance().getReference("Kullanicilar")

        binding.textViewTitle.text = "Hoş Geldin, $girisYapanKullaniciAdi"

        loadKullaniciBilgileri(girisYapanKullaniciAdi)

        binding.bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_odas -> {
                    showScreen(Screen.ROOM_LIST)
                    true
                }
                R.id.nav_rezervasyonlarim -> {
                    showScreen(Screen.RESERVATIONS)
                    true
                }
                R.id.nav_profil -> {
                    showScreen(Screen.PROFILE)
                    true
                }
                else -> false
            }
        }

        binding.btnCikisYap.setOnClickListener { cikisYap() }
        binding.bottomNavigation.selectedItemId = R.id.nav_odas
    }

    private fun loadKullaniciBilgileri(kullaniciAdi: String) {
        databaseKullanicilar.orderByChild("kullaniciAdi").equalTo(kullaniciAdi).addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val user = snapshot.children.first().getValue(Users::class.java)
                    user?.let {
                        kullaniciAdSoyad = "${it.ad ?: ""} ${it.soyad ?: ""}"

                        // Profil Ekranındaki TextView'ları güncelle
                        binding.tvProfilKullaniciAdi.text = it.kullaniciAdi
                        binding.tvProfilIsim.text = "Ad Soyad: $kullaniciAdSoyad"

                        // TELEFON KONTROLÜ
                        if (!it.telefonNumarasi.isNullOrEmpty()) {
                            binding.tvProfilTelefon.text = "Telefon: ${it.telefonNumarasi}"
                        } else {
                            binding.tvProfilTelefon.text = ""
                        }
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@KullaniciEkrani, "Hata!", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun cikisYap() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun showScreen(screen: Screen) {
        binding.rvKullaniciOdaListesi.visibility = View.GONE
        binding.rvAktifRezervasyonlarim.visibility = View.GONE
        binding.profilContainer.visibility = View.GONE

        when (screen) {
            Screen.ROOM_LIST -> {
                binding.rvKullaniciOdaListesi.visibility = View.VISIBLE
                loadOdaListesi()
            }
            Screen.RESERVATIONS -> {
                binding.rvAktifRezervasyonlarim.visibility = View.VISIBLE
                loadAktifRezervasyonlar(girisYapanKullaniciAdi)
            }
            Screen.PROFILE -> {
                binding.profilContainer.visibility = View.VISIBLE
            }
        }
    }

    private fun loadOdaListesi() {
        databaseOdalar.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val odaList = arrayListOf<Oda>()
                for (odaSnapshot in snapshot.children) {
                    val oda = odaSnapshot.getValue(Oda::class.java)
                    oda?.let { odaList.add(it) }
                }

                val odaAdapter = OdaAdapter(
                    context = this@KullaniciEkrani,
                    odaList = odaList,
                    isDeleteMode = false,
                    isPersonnelMode = false,
                    temizlikListener = null,
                    onClick = { oda ->
                        if (oda.temizlikDurumu == "Kirli") {
                            Toast.makeText(
                                this@KullaniciEkrani,
                                "Oda şu an temizleniyor.",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            showRezervasyonForm(oda)
                        }
                    }
                )
                binding.rvKullaniciOdaListesi.layoutManager = LinearLayoutManager(this@KullaniciEkrani)
                binding.rvKullaniciOdaListesi.adapter = odaAdapter
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun loadAktifRezervasyonlar(kullaniciAdi: String) {
        databaseRezervasyonlar.orderByChild("kullaniciAdi").equalTo(kullaniciAdi)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val rezervasyonList = arrayListOf<Rezervasyon>()
                    for (rezSnapshot in snapshot.children) {
                        val rez = rezSnapshot.getValue(Rezervasyon::class.java)
                        rez?.let {
                            if (it.durum != "iptal edildi") {
                                it.rezervasyonId = rezSnapshot.key
                                rezervasyonList.add(it)
                            }
                        }
                    }
                    val adapter = RezervasyonAdapter(
                        this@KullaniciEkrani,
                        rezervasyonList,
                        this@KullaniciEkrani
                    )
                    binding.rvAktifRezervasyonlarim.layoutManager = LinearLayoutManager(this@KullaniciEkrani)
                    binding.rvAktifRezervasyonlarim.adapter = adapter
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    override fun onRezervasyonIptal(rezervasyon: Rezervasyon) {
        rezervasyonIptalEt(rezervasyon)
    }

    private fun rezervasyonIptalEt(rezervasyon: Rezervasyon) {
        val rezId = rezervasyon.rezervasyonId
        AlertDialog.Builder(this)
            .setTitle("Rezervasyon İptali")
            .setMessage("İptal etmek istediğinizden emin misiniz?")
            .setPositiveButton("Evet") { _, _ ->
                databaseRezervasyonlar.child(rezId!!).child("durum").setValue("iptal edildi")
                    .addOnSuccessListener {
                        Toast.makeText(this, "Rezervasyon İptal Edildi", Toast.LENGTH_SHORT).show()
                        loadAktifRezervasyonlar(girisYapanKullaniciAdi)
                    }
            }.setNegativeButton("Hayır", null).show()
    }

    private fun showRezervasyonForm(oda: Oda) {
        val formBinding = DialogRezervasyonFormuBinding.inflate(LayoutInflater.from(this))
        val dialog = AlertDialog.Builder(this)
            .setView(formBinding.root)
            .create()

        formBinding.tvRezervasyonBaslik.text = "Oda #${oda.odaNo} Rezervasyonu"


        formBinding.tvDoluTarihlerBilgi.text = "📅 Doluluk Durumu Kontrol Ediliyor..."

        databaseRezervasyonlar.orderByChild("odaNo").equalTo(oda.odaNo)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val doluListesi = StringBuilder()
                    var doluVarMi = false

                    for (rez in snapshot.children) {
                        val r = rez.getValue(Rezervasyon::class.java)
                        // Sadece iptal edilmemiş rezervasyonları dikkate al
                        if (r != null && r.durum != "iptal edildi") {
                            doluVarMi = true
                            doluListesi.append("❌ ${r.girisTarihi} ile ${r.cikisTarihi} arası\n")
                        }
                    }

                    if (doluVarMi) {
                        formBinding.tvDoluTarihlerBilgi.text = "⚠️ BU ODA ŞU TARİHLERDE DOLU:\n\n$doluListesi"
                        formBinding.tvDoluTarihlerBilgi.setTextColor(Color.RED)
                    } else {
                        formBinding.tvDoluTarihlerBilgi.text = "✅ Bu oda şu an tamamen müsait."
                        formBinding.tvDoluTarihlerBilgi.setTextColor(Color.parseColor("#388E3C")) // Yeşil renk
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    formBinding.tvDoluTarihlerBilgi.text = "Müsaitlik bilgisi alınamadı."
                }
            })

        formBinding.etGirisTarihi.setOnClickListener { showDatePicker(formBinding.etGirisTarihi, formBinding, oda) }
        formBinding.etCikisTarihi.setOnClickListener { showDatePicker(formBinding.etCikisTarihi, formBinding, oda) }

        formBinding.btnRezervasyonYap.setOnClickListener {
            val girisTarihi = formBinding.etGirisTarihi.text.toString()
            val cikisTarihi = formBinding.etCikisTarihi.text.toString()
            val toplamFiyat = formBinding.tvToplamFiyat.tag as? Double

            if (girisTarihi.isEmpty() || cikisTarihi.isEmpty() || toplamFiyat == null || toplamFiyat <= 0.0) {
                Toast.makeText(this, "Lütfen geçerli tarih seçin.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            formBinding.btnRezervasyonYap.isEnabled = false
            formBinding.btnRezervasyonYap.text = "İşleniyor..."
            formBinding.btnRezervasyonYap.setBackgroundColor(Color.GRAY)

            checkOdaMusaitlik(oda.odaNo!!, girisTarihi, cikisTarihi) { isMusait ->
                if (isMusait) {
                    rezervasyonYap(oda, girisTarihi, cikisTarihi, toplamFiyat, dialog)
                } else {
                    Toast.makeText(this, "⚠️ Seçtiğiniz tarihlerde oda dolu! Listeyi kontrol edin.", Toast.LENGTH_LONG).show()
                    formBinding.btnRezervasyonYap.isEnabled = true
                    formBinding.btnRezervasyonYap.text = "REZERVASYON YAP"
                    formBinding.btnRezervasyonYap.setBackgroundColor(Color.parseColor("#00A5B5"))
                }
            }
        }
        dialog.show()
    }

    private fun showDatePicker(target: EditText, formBinding: DialogRezervasyonFormuBinding, oda: Oda) {
        val c = Calendar.getInstance()
        DatePickerDialog(this, { _, y, m, d ->
            val selected = Calendar.getInstance().apply { set(y, m, d) }
            target.setText(dateFormat.format(selected.time))
            calculateAndCheckMüsaitlik(formBinding, oda)
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).apply {
            datePicker.minDate = System.currentTimeMillis()
            show()
        }
    }

    private fun calculateAndCheckMüsaitlik(formBinding: DialogRezervasyonFormuBinding, oda: Oda) {
        val gStr = formBinding.etGirisTarihi.text.toString()
        val cStr = formBinding.etCikisTarihi.text.toString()

        if (gStr.isNotEmpty() && cStr.isNotEmpty()) {
            val gDate = dateFormat.parse(gStr)
            val cDate = dateFormat.parse(cStr)

            if (gDate != null && cDate != null && cDate.after(gDate)) {
                val gun = TimeUnit.DAYS.convert(cDate.time - gDate.time, TimeUnit.MILLISECONDS)
                checkOdaMusaitlik(oda.odaNo!!, gStr, cStr) { isMusait ->
                    if (isMusait) {
                        val toplam = (oda.fiyat ?: 0.0) * gun
                        formBinding.tvToplamFiyat.text = "Toplam: ${String.format("%.2f TL", toplam)}"
                        formBinding.tvToplamFiyat.setTextColor(Color.parseColor("#4CAF50"))
                        formBinding.tvToplamFiyat.tag = toplam
                    } else {
                        formBinding.tvToplamFiyat.text = "Bu tarihlerde DOLU"
                        formBinding.tvToplamFiyat.setTextColor(Color.RED)
                        formBinding.tvToplamFiyat.tag = 0.0
                    }
                }
            }
        }
    }

    private fun checkOdaMusaitlik(odaNo: String, g: String, c: String, cb: (Boolean) -> Unit) {
        databaseRezervasyonlar.orderByChild("odaNo").equalTo(odaNo).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var ok = true
                val nG = dateFormat.parse(g)!!.time
                val nC = dateFormat.parse(c)!!.time
                for (sn in snapshot.children) {
                    val r = sn.getValue(Rezervasyon::class.java)
                    if (r != null && r.durum != "iptal edildi") {
                        val mG = dateFormat.parse(r.girisTarihi!!)!!.time
                        val mC = dateFormat.parse(r.cikisTarihi!!)!!.time
                        if (nG < mC && nC > mG) {
                            ok = false
                            break
                        }
                    }
                }
                cb(ok)
            }
            override fun onCancelled(error: DatabaseError) = cb(false)
        })
    }

    private fun rezervasyonYap(oda: Oda, g: String, c: String, fiyat: Double, dialog: AlertDialog) {
        val rezKey = databaseRezervasyonlar.push().key ?: return
        val yeni = Rezervasyon(
            rezervasyonId = rezKey,
            odaNo = oda.odaNo,
            kullaniciAdi = girisYapanKullaniciAdi,
            girisTarihi = g,
            cikisTarihi = c,
            toplamFiyat = fiyat
        )

        databaseRezervasyonlar.child(rezKey).setValue(yeni).addOnSuccessListener {
            Toast.makeText(this, "Rezervasyon Başarılı", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
            showScreen(Screen.ROOM_LIST)
        }
    }
}