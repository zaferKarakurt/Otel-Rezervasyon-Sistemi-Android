package com.zafer.otelmaris.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.zafer.otelmaris.R
import com.zafer.otelmaris.model.Oda
import com.zafer.otelmaris.view.TemizlikDurumuListener
import java.util.Locale


class OdaAdapter(
    private val context: Context,
    private val odaList: List<Oda>,
    private val isDeleteMode: Boolean,
    private val isPersonnelMode: Boolean,
    private val temizlikListener: TemizlikDurumuListener?,
    private val onClick: (Oda) -> Unit
) : RecyclerView.Adapter<OdaAdapter.OdaViewHolder>() {

    class OdaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvOdaNo: TextView = itemView.findViewById(R.id.tvOdaNo)
        val tvOdaTip: TextView = itemView.findViewById(R.id.tvOdaTip)
        val tvFiyat: TextView = itemView.findViewById(R.id.tvFiyat)
        val tvDurum: TextView = itemView.findViewById(R.id.tvDurum)
        val tvSil: TextView = itemView.findViewById(R.id.tvSil)
        val tvTemizlikGuncelle: TextView = itemView.findViewById(R.id.tvTemizlikGuncelle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OdaViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.list_item_oda, parent, false)
        return OdaViewHolder(view)
    }

    override fun onBindViewHolder(holder: OdaViewHolder, position: Int) {
        val oda = odaList[position]

        // ... (Mevcut gösterim kodları)
        holder.tvOdaNo.text = "ODA #${oda.odaNo ?: "N/A"}"
        holder.tvOdaTip.text = "${oda.tip ?: "Bilinmiyor"}"
        holder.tvFiyat.text = String.format(Locale.getDefault(), "%.2f TL", oda.fiyat ?: 0.0)

        val musaitDurum = if (oda.musaitMi == true) "Müsait" else "DOLU"
        val temizlikDurum = oda.temizlikDurumu ?: "Kirli"

        holder.tvDurum.text = "$musaitDurum / $temizlikDurum"

        if (oda.musaitMi == false) {
            holder.tvDurum.setTextColor(Color.RED)
        } else if (temizlikDurum == "Kirli") {
            holder.tvDurum.setTextColor(Color.parseColor("#FF9800"))
        } else {
            holder.tvDurum.setTextColor(Color.parseColor("#4CAF50"))
        }

        if (isDeleteMode) {
            holder.tvSil.visibility = View.VISIBLE
            holder.tvTemizlikGuncelle.visibility = View.GONE
            holder.tvSil.setOnClickListener {
                onClick(oda)
            }
        }

        // --- PERSONEL MODU KONTROLÜ ---
        else if (isPersonnelMode) {
            holder.tvSil.visibility = View.GONE
            holder.tvFiyat.visibility = View.GONE
            holder.tvTemizlikGuncelle.visibility = View.VISIBLE


            holder.tvTemizlikGuncelle.text = if (temizlikDurum == "Temiz") "KİRLİ" else "TEMİZ"
            holder.tvTemizlikGuncelle.setTextColor(if (temizlikDurum == "Temiz") Color.RED else Color.parseColor("#4CAF50"))

            // Tıklama Olayı: Durumu tersine çevirip Listener'a gönder
            holder.tvTemizlikGuncelle.setOnClickListener {
                if (temizlikListener != null) {
                    val yeniDurum = if (temizlikDurum == "Temiz") "Kirli" else "Temiz"
                    temizlikListener.onTemizlikDurumuGuncelle(oda.odaNo ?: "", yeniDurum)
                }
            }
            holder.itemView.setOnClickListener(null)
        }

        // --- KULLANICI MODU KONTROLÜ ---
        else {
            holder.tvSil.visibility = View.GONE
            holder.tvTemizlikGuncelle.visibility = View.GONE
            holder.itemView.setOnClickListener {
                onClick(oda)
            }
        }
    }

    override fun getItemCount(): Int = odaList.size
}