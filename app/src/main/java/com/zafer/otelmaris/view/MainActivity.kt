package com.zafer.otelmaris.view

import android.content.Intent
import android.os.Bundle
//import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
//import com.google.firebase.database.DatabaseReference
//import com.google.firebase.database.FirebaseDatabase
import com.zafer.otelmaris.databinding.ActivityMainBinding
import androidx.appcompat.app.AppCompatDelegate
import androidx.cardview.widget.CardView
import com.zafer.otelmaris.R

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.btnMainGirisYap.setOnClickListener {
            val yeniIntent = Intent(this@MainActivity, GirisYap::class.java)
            startActivity(yeniIntent)
        }

        binding.btnMainKayitOl.setOnClickListener {
            val yeniIntent = Intent(this@MainActivity, KayitOl::class.java)
            startActivity(yeniIntent)
        }


        val cardHakkimizda = findViewById<CardView>(R.id.cardHakkimizda)
        binding.cardHakkimizda.setOnClickListener {
            val yeniIntent = Intent(this@MainActivity, HakkimizdaActivity::class.java)
            startActivity(yeniIntent)
        }

   }
}