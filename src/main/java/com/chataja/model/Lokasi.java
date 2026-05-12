package com.chataja.model;

/**
 * Model untuk Lokasi Gereja.
 * Sesuai ERD: id_lokasi, nama_tempat, alamat, kontak, id_user.
 */
public class Lokasi {
    private String idLokasi;
    private String namaTempat;
    private String alamat;
    private String kontak;
    private String idUser;

    public Lokasi() {}

    public Lokasi(String idLokasi, String namaTempat,
                  String alamat, String kontak, String idUser) {
        this.idLokasi = idLokasi;
        this.namaTempat = namaTempat;
        this.alamat = alamat;
        this.kontak = kontak;
        this.idUser = idUser;
    }

    /** Format tampilan untuk chatbot */
    public String tampilkan() {
        return String.format("📍 %s\n  Alamat : %s\n  Kontak : %s",
                namaTempat, alamat, kontak);
    }

    // ===================== Getters & Setters =====================
    public String getIdLokasi() { return idLokasi; }
    public void setIdLokasi(String idLokasi) { this.idLokasi = idLokasi; }

    public String getNamaTempat() { return namaTempat; }
    public void setNamaTempat(String namaTempat) { this.namaTempat = namaTempat; }

    public String getAlamat() { return alamat; }
    public void setAlamat(String alamat) { this.alamat = alamat; }

    public String getKontak() { return kontak; }
    public void setKontak(String kontak) { this.kontak = kontak; }

    public String getIdUser() { return idUser; }
    public void setIdUser(String idUser) { this.idUser = idUser; }

    @Override
    public String toString() { return namaTempat; }
}
