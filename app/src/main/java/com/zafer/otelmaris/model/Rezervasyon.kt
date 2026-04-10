package com.zafer.otelmaris.model

data class Rezervasyon(
    var rezervasyonId: String? = null,
    var odaNo: String? = null,
    var kullaniciAdi: String? = null,
    var girisTarihi: String? = null,
    var cikisTarihi: String? = null,
    var toplamFiyat: Double? = 0.0,
    var durum: String? = "onaylandı"
) {

}