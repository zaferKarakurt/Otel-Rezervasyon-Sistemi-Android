package com.zafer.otelmaris.model

class Users {

    var ad: String? = null
    var soyad: String? = null
    var telefonNumarasi: String? = null
    var kullaniciAdi: String? = null
    var sifre: String? = null
    var rol: String? = null
    // ------------------------------------

    //  Firebase'in okuma yapabilmesi için boş constructor
    constructor() {
        // İçini boş
    }

    // 2. KAYIT OLMA EKRANINDA KULLANILAN CONSTRUCTOR
    constructor(ad: String, soyad: String, telefonNumarasi: String, kullaniciAdi: String, sifre: String) {
        this.ad = ad
        this.soyad = soyad
        this.telefonNumarasi = telefonNumarasi
        this.kullaniciAdi = kullaniciAdi
        this.sifre = sifre
        this.rol = "user"
    }
}