package com.chataja.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Model untuk Pengumuman Gereja.
 * Sesuai ERD: id_pengumuman, judul, isi, tanggal, id_user.
 */
public class Pengumuman {
    private String idPengumuman;
    private String judul;
    private String isi;
    private LocalDate tanggal;
    private String idUser;
    private String fotoPath;

    public Pengumuman() {}

    public Pengumuman(String idPengumuman, String judul,
                      String isi, LocalDate tanggal, String idUser) {
        this.idPengumuman = idPengumuman;
        this.judul = judul;
        this.isi = isi;
        this.tanggal = tanggal;
        this.idUser = idUser;
    }

    /** Format tampilan untuk chatbot */
    public String tampilkan() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMMM yyyy", new Locale("id", "ID"));
        return String.format("📢 %s\n  Tanggal : %s\n  %s",
                judul,
                tanggal != null ? tanggal.format(fmt) : "-",
                isi);
    }

    // ===================== Getters & Setters =====================
    public String getIdPengumuman() { return idPengumuman; }
    public void setIdPengumuman(String idPengumuman) { this.idPengumuman = idPengumuman; }

    public String getJudul() { return judul; }
    public void setJudul(String judul) { this.judul = judul; }

    public String getIsi() { return isi; }
    public void setIsi(String isi) { this.isi = isi; }

    public LocalDate getTanggal() { return tanggal; }
    public void setTanggal(LocalDate tanggal) { this.tanggal = tanggal; }

    public String getIdUser() { return idUser; }
    public void setIdUser(String idUser) { this.idUser = idUser; }

    public String getFotoPath() { return fotoPath; }
    public void setFotoPath(String fotoPath) { this.fotoPath = fotoPath; }

    // cek foto ada apa ga
    public boolean hasFoto(){
        return fotoPath != null && !fotoPath.isBlank();
    }

    public String getTanggalStr() {
        if (tanggal == null) return "-";
        return tanggal.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }
}
