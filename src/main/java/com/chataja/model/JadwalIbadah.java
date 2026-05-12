package com.chataja.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Model untuk Jadwal Ibadah gereja.
 * Sesuai ERD: id_jadwal, nama_ibadah, tanggal, waktu, id_lokasi, id_user.
 */
public class JadwalIbadah {
    private String idJadwal;
    private String namaIbadah;
    private LocalDate tanggal;
    private LocalTime waktu;
    private String idLokasi;
    private String namaLokasi;  // join field
    private String idUser;

    public JadwalIbadah() {}

    public JadwalIbadah(String idJadwal, String namaIbadah,
                        LocalDate tanggal, LocalTime waktu,
                        String idLokasi, String idUser) {
        this.idJadwal = idJadwal;
        this.namaIbadah = namaIbadah;
        this.tanggal = tanggal;
        this.waktu = waktu;
        this.idLokasi = idLokasi;
        this.idUser = idUser;
    }

    /** Format tampilan untuk chatbot */
    public String tampilkan() {
        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd MMMM yyyy",
                new java.util.Locale("id", "ID"));
        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");
        return String.format("• %s\n  Tanggal : %s\n  Waktu   : %s WIB\n  Lokasi  : %s",
                namaIbadah,
                tanggal != null ? tanggal.format(dateFmt) : "-",
                waktu != null ? waktu.format(timeFmt) : "-",
                namaLokasi != null ? namaLokasi : idLokasi);
    }

    // ===================== Getters & Setters =====================
    public String getIdJadwal() { return idJadwal; }
    public void setIdJadwal(String idJadwal) { this.idJadwal = idJadwal; }

    public String getNamaIbadah() { return namaIbadah; }
    public void setNamaIbadah(String namaIbadah) { this.namaIbadah = namaIbadah; }

    public LocalDate getTanggal() { return tanggal; }
    public void setTanggal(LocalDate tanggal) { this.tanggal = tanggal; }

    public LocalTime getWaktu() { return waktu; }
    public void setWaktu(LocalTime waktu) { this.waktu = waktu; }

    public String getIdLokasi() { return idLokasi; }
    public void setIdLokasi(String idLokasi) { this.idLokasi = idLokasi; }

    public String getNamaLokasi() { return namaLokasi; }
    public void setNamaLokasi(String namaLokasi) { this.namaLokasi = namaLokasi; }

    public String getIdUser() { return idUser; }
    public void setIdUser(String idUser) { this.idUser = idUser; }

    public String getWaktuStr() {
        return waktu != null ? waktu.format(DateTimeFormatter.ofPattern("HH:mm")) + " WIB" : "-";
    }

    public String getTanggalStr() {
        if (tanggal == null) return "-";
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return tanggal.format(fmt);
    }
}
