package com.zafer.otelmaris.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.zafer.otelmaris.databinding.ActivityGirisYapBinding
import com.zafer.otelmaris.model.Users

class GirisYap : AppCompatActivity() {

    private lateinit var binding: ActivityGirisYapBinding
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityGirisYapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Veritabanı yolu
        database = FirebaseDatabase.getInstance().getReference("Kullanicilar")

        binding.tvGecisKayitOl.setOnClickListener {
            val yeniIntent = Intent(this@GirisYap, KayitOl::class.java)
            startActivity(yeniIntent)
        }

        binding.btnGYGirisYap.setOnClickListener {

            val girilenKullaniciAdi = binding.etGYKullaniciAdi.text.toString().trim()
            val girilenSifre = binding.etGYSifre.text.toString().trim()

            if (girilenKullaniciAdi.isNotEmpty() && girilenSifre.isNotEmpty()) {

                database.orderByChild("kullaniciAdi")
                    .equalTo(girilenKullaniciAdi)
                    .addListenerForSingleValueEvent(object : ValueEventListener {

                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                var girisBasarili = false

                                for (kullaniciSnapshot in snapshot.children) {
                                    val user = kullaniciSnapshot.getValue(Users::class.java)

                                    if (user?.sifre == girilenSifre) {
                                        girisBasarili = true

                                        // Kullanıcı Adını al
                                        val girisYapanKullaniciAdi = user.kullaniciAdi ?: "Bilinmeyen"


                                        if (user.rol == "admin") {
                                            // ADMIN ROLÜ İSE
                                            Toast.makeText(this@GirisYap, "✅ Admin Girişi Başarılı!", Toast.LENGTH_LONG).show()
                                            val intent = Intent(this@GirisYap, AdminEkrani::class.java)
                                            intent.putExtra("kullaniciAdi", girisYapanKullaniciAdi)
                                            startActivity(intent)

                                        } else if (user.rol == "personel") {
                                            // PERSONEL ROLÜ İSE
                                            Toast.makeText(this@GirisYap, "✅ Personel Girişi Başarılı! Hoş geldin ${user.ad}", Toast.LENGTH_LONG).show()
                                            val intent = Intent(this@GirisYap, PersonelEkrani::class.java) // YENİ EKRANA YÖNLENDİRME
                                            intent.putExtra("kullaniciAdi", girisYapanKullaniciAdi)
                                            startActivity(intent)

                                        } else {
                                            // NORMAL KULLANICI ROLÜ İSE (ya da rol tanımlanmamışsa)
                                            Toast.makeText(this@GirisYap, "✅ Giriş Başarılı! Hoş geldin ${user.ad}", Toast.LENGTH_LONG).show()
                                            val intent = Intent(this@GirisYap, KullaniciEkrani::class.java)
                                            intent.putExtra("kullaniciAdi", girisYapanKullaniciAdi) // Kullanıcı Adını Gönder
                                            startActivity(intent)
                                        }

                                        break
                                    }
                                }

                                if (!girisBasarili) {
                                    Toast.makeText(this@GirisYap, "❌ Hata: Şifre yanlış!", Toast.LENGTH_SHORT).show()
                                }

                            } else {
                                Toast.makeText(this@GirisYap, "❌ Hata: Kullanıcı adı bulunamadı!", Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(this@GirisYap, "Database Hatası: ${error.message}", Toast.LENGTH_LONG).show()
                        }
                    })
            } else {
                Toast.makeText(this, "Lütfen tüm alanları doldurun.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}