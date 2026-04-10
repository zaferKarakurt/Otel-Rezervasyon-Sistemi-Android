package com.zafer.otelmaris.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.*
import com.zafer.otelmaris.adapter.OdaAdapter
import com.zafer.otelmaris.databinding.ActivityPersonelEkraniBinding
import com.zafer.otelmaris.model.Oda

// Temizlik Durumu Güncelleme için bir Listener tanımlama
interface TemizlikDurumuListener {
    fun onTemizlikDurumuGuncelle(odaNo: String, yeniDurum: String)
}


class PersonelEkrani : AppCompatActivity(), TemizlikDurumuListener {

    private lateinit var binding: ActivityPersonelEkraniBinding
    private lateinit var databaseOdalar: DatabaseReference
    private var girisYapanKullaniciAdi: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPersonelEkraniBinding.inflate(layoutInflater)
        setContentView(binding.root)

        girisYapanKullaniciAdi = intent.getStringExtra("kullaniciAdi") ?: "Bilinmeyen"

        databaseOdalar = FirebaseDatabase.getInstance().getReference("Odalar")

        binding.textViewTitle.text = "Personel Paneli - ${girisYapanKullaniciAdi}"


        binding.btnPersonelCikisYap.setOnClickListener {
            cikisYap()
        }
        // Oda Listesini Yükle
        loadOdaListesi()
    }

    private fun cikisYap() {
        Toast.makeText(this, "Çıkış Yapılıyor...", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    // --- ODA LİSTELEME ---
    private fun loadOdaListesi() {
        databaseOdalar.addValueEventListener(object : ValueEventListener {
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
                    context = this@PersonelEkrani,
                    odaList = odaList,
                    isDeleteMode = false,
                    isPersonnelMode = true,
                    temizlikListener = this@PersonelEkrani,
                    onClick = { }
                )

                binding.rvPersonelOdaListesi.layoutManager = LinearLayoutManager(this@PersonelEkrani)
                binding.rvPersonelOdaListesi.adapter = odaAdapter
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@PersonelEkrani, "Odalar yüklenemedi: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onTemizlikDurumuGuncelle(odaNo: String, yeniDurum: String) {
        val odaRef = databaseOdalar.child(odaNo).child("temizlikDurumu")

        odaRef.setValue(yeniDurum)
            .addOnSuccessListener {
                Toast.makeText(this, "${odaNo} odasının durumu ${yeniDurum} olarak güncellendi.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Güncelleme başarısız: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}