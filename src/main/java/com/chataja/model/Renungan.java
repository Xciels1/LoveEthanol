package com.chataja.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Model untuk Renungan Harian gereja.
 * Sesuai ERD: id_renungan, judul, isi, tanggal, id_user.
 */
public class Renungan {
    private String idRenungan;
    private String judul;
    private String isi;
    private LocalDate tanggal;
    private String idUser;

    public Renungan() {}

    public Renungan(String idRenungan, String judul,
                    String isi, LocalDate tanggal, String idUser) {
        this.idRenungan = idRenungan;
        this.judul = judul;
        this.isi = isi;
        this.tanggal = tanggal;
        this.idUser = idUser;
    }

    /** Format tampilan untuk chatbot */
    public String tampilkan() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMMM yyyy", new Locale("id", "ID"));
        return String.format("📖 RENUNGAN HARIAN\n\n" +
                        "Tanggal : %s\n" +
                        "Judul   : %s\n\n" +
                        "%s",
                tanggal != null ? tanggal.format(fmt) : "-",
                judul,
                isi);
    }

    // ===================== Getters & Setters =====================
    public String getIdRenungan() { return idRenungan; }
    public void setIdRenungan(String idRenungan) { this.idRenungan = idRenungan; }

    public String getJudul() { return judul; }
    public void setJudul(String judul) { this.judul = judul; }

    public String getIsi() { return isi; }
    public void setIsi(String isi) { this.isi = isi; }

    public LocalDate getTanggal() { return tanggal; }
    public void setTanggal(LocalDate tanggal) { this.tanggal = tanggal; }

    public String getIdUser() { return idUser; }
    public void setIdUser(String idUser) { this.idUser = idUser; }

    public String getTanggalStr() {
        if (tanggal == null) return "-";
        return tanggal.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }
}
