package com.zafer.otelmaris.view

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
// Realtime Database ve View Binding sınıfları
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.zafer.otelmaris.databinding.ActivityKayitOlBinding
import com.zafer.otelmaris.model.Users

class KayitOl : AppCompatActivity() {

    private lateinit var binding: ActivityKayitOlBinding // View Binding tanımı
    private lateinit var database: DatabaseReference // Database referansı

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityKayitOlBinding.inflate(layoutInflater)
        setContentView(binding.root)


        database = FirebaseDatabase.getInstance().getReference("Kullanicilar")

        binding.btnKayitOl.setOnClickListener {


            val ad = binding.etAd.text.toString()
            val soyad = binding.etSoyad.text.toString()
            val telefon = binding.etTelefonNumrasi.text.toString()
            val kullaniciAdi = binding.etKullaniciAdi.text.toString()
            val sifre = binding.etSifre.text.toString()


            if ( sifre.isNotEmpty() && kullaniciAdi.isNotEmpty() && ad.isNotEmpty() && soyad.isNotEmpty() && telefon.isNotEmpty()) {


                val user = Users(ad, soyad, telefon, kullaniciAdi, sifre)
                database.push().setValue(user)
                    .addOnSuccessListener {
                        Toast.makeText(this, "✅ Kullanıcı başarıyla eklendi!", Toast.LENGTH_SHORT).show()
                        binding.etAd.setText("")
                        binding.etSoyad.setText("")
                        binding.etTelefonNumrasi.setText("")
                        binding.etKullaniciAdi.setText("")
                        binding.etSifre.setText("")
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "❌ HATA: Kayıt başarısız oldu.", Toast.LENGTH_LONG).show()
                    }
            } else {
                Toast.makeText(this, "Lütfen tüm alanları doğru doldurun.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}