package com.chataja.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Model untuk Jadwal Tugas Majelis.
 * Sesuai ERD: id_tugas, tanggal, tugas, id_user (majelis yang bertugas), waktu_ibadah.
 */
public class JadwalTugas {
    private String idTugas;
    private LocalDate tanggal;
    private String tugas;
    private String idUser;        // FK ke tabel users (majelis)
    private String namaMajelis;   // join field
    private LocalTime waktuIbadah;

    public JadwalTugas() {}

    public JadwalTugas(String idTugas, LocalDate tanggal,
                       String tugas, String idUser) {
        this.idTugas = idTugas;
        this.tanggal = tanggal;
        this.tugas = tugas;
        this.idUser = idUser;
    }

    /** Format tampilan untuk chatbot (majelis yang login melihat jadwalnya sendiri) */
    public String tampilkan() {
        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd MMMM yyyy", new Locale("id", "ID"));
        String waktuStr = (waktuIbadah != null)
                ? " pukul " + waktuIbadah.format(DateTimeFormatter.ofPattern("HH:mm")) + " WIB"
                : "";
        return String.format("• %s\n  Tanggal : %s%s",
                tugas,
                tanggal != null ? tanggal.format(dateFmt) : "-",
                waktuStr);
    }

    // ===================== Getters & Setters =====================
    public String getIdTugas() { return idTugas; }
    public void setIdTugas(String idTugas) { this.idTugas = idTugas; }

    public LocalDate getTanggal() { return tanggal; }
    public void setTanggal(LocalDate tanggal) { this.tanggal = tanggal; }

    public String getTugas() { return tugas; }
    public void setTugas(String tugas) { this.tugas = tugas; }

    public String getIdUser() { return idUser; }
    public void setIdUser(String idUser) { this.idUser = idUser; }

    public String getNamaMajelis() { return namaMajelis; }
    public void setNamaMajelis(String namaMajelis) { this.namaMajelis = namaMajelis; }

    public LocalTime getWaktuIbadah() { return waktuIbadah; }
    public void setWaktuIbadah(LocalTime waktuIbadah) { this.waktuIbadah = waktuIbadah; }

    public String getTanggalStr() {
        if (tanggal == null) return "-";
        return tanggal.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    public String getWaktuStr() {
        return waktuIbadah != null
                ? waktuIbadah.format(DateTimeFormatter.ofPattern("HH:mm")) + " WIB"
                : "-";
    }
}
