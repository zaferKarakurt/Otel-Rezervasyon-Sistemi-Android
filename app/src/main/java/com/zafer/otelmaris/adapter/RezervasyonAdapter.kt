package com.zafer.otelmaris.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.zafer.otelmaris.R
import com.zafer.otelmaris.model.Rezervasyon
import com.zafer.otelmaris.view.RezervasyonIptalListener
import java.util.*

class RezervasyonAdapter(
    private val context: Context,
    private var rezervasyonList: List<Rezervasyon>,
    private val iptalListener: RezervasyonIptalListener? = null
) : RecyclerView.Adapter<RezervasyonAdapter.RezervasyonViewHolder>() {

    class RezervasyonViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvOdaNo: TextView = view.findViewById(R.id.tv_rez_oda_no)
        val tvMusteriAd: TextView = view.findViewById(R.id.tv_rez_musteri_adi)
        val tvTarihAraligi: TextView = view.findViewById(R.id.tv_rez_tarih_araligi)
        val tvToplamFiyat: TextView = view.findViewById(R.id.tv_rez_toplam_fiyat)
        val btnIptalEt: Button = view.findViewById(R.id.btn_rez_iptal)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RezervasyonViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_rezervasyon, parent, false)
        return RezervasyonViewHolder(view)
    }

    override fun onBindViewHolder(holder: RezervasyonViewHolder, position: Int) {
        val rezervasyon = rezervasyonList[position]

        holder.tvOdaNo.text = "Oda No: #${rezervasyon.odaNo}"

        // YENİ: Müşteri adını gösteriyoruz
        holder.tvMusteriAd.text = rezervasyon.kullaniciAdi ?: "İsimsiz Müşteri"

        holder.tvTarihAraligi.text = "Giriş: ${rezervasyon.girisTarihi} / Çıkış: ${rezervasyon.cikisTarihi}"

        val fiyatFormatted = String.format(Locale.getDefault(), "%.2f TL", rezervasyon.toplamFiyat ?: 0.0)
        holder.tvToplamFiyat.text = "Tutar: $fiyatFormatted"

        if (iptalListener == null) {
            holder.btnIptalEt.visibility = View.GONE
        } else {
            holder.btnIptalEt.visibility = View.VISIBLE
            holder.btnIptalEt.setOnClickListener {
                iptalListener?.onRezervasyonIptal(rezervasyon)
            }
        }
    }

    override fun getItemCount(): Int = rezervasyonList.size

    fun listeyiGuncelle(yeniListe: List<Rezervasyon>) {
        this.rezervasyonList = yeniListe
        notifyDataSetChanged()
    }
}