package com.zafer.otelmaris.model

class Oda {

    var odaNo: String? = null
    var tip: String? = null
    var fiyat: Double? = null
    var temizlikDurumu: String? = null
    var musaitMi: Boolean? = null

    constructor() {
    }

    constructor(odaNo: String, tip: String, fiyat: Double, temizlikDurumu: String, musaitMi: Boolean) {
        this.odaNo = odaNo
        this.tip = tip
        this.fiyat = fiyat
        this.temizlikDurumu = temizlikDurumu
        this.musaitMi = musaitMi
    }
}