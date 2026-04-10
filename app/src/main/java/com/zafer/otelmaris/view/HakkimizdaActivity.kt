package com.zafer.otelmaris.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.zafer.otelmaris.databinding.ActivityHakkimizdaBinding

class HakkimizdaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHakkimizdaBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Binding Kurulumu
        binding = ActivityHakkimizdaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Koyu Modu Kapatma
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        // Geri Dön Butonu
        binding.btnGeriDon.setOnClickListener {
            finish()
        }
    }
}