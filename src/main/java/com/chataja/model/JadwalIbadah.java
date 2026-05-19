package com.chataja.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Model untuk Jadwal Ibadah gereja.
 * Ditambah: namaPendeta — nama pendeta/pemimpin ibadah.
 */
public class JadwalIbadah {
    private String idJadwal;
    private String namaIbadah;
    private LocalDate tanggal;
    private LocalTime waktu;
    private String idLokasi;
    private String namaLokasi;   // join field
    private String idUser;
    private String namaPendeta;  // ← BARU: nama pendeta yang bertugas

    public JadwalIbadah() {}

    public JadwalIbadah(String idJadwal, String namaIbadah,
                        LocalDate tanggal, LocalTime waktu,
                        String idLokasi, String idUser) {
        this.idJadwal   = idJadwal;
        this.namaIbadah = namaIbadah;
        this.tanggal    = tanggal;
        this.waktu      = waktu;
        this.idLokasi   = idLokasi;
        this.idUser     = idUser;
    }

    /** Format tampilan untuk chatbot — termasuk informasi pendeta */
    public String tampilkan() {
        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd MMMM yyyy",
                new java.util.Locale("id", "ID"));
        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");

        String pendeta = (namaPendeta != null && !namaPendeta.isBlank())
                ? namaPendeta : "-";

        return String.format(
                "• %s\n  Tanggal : %s\n  Waktu   : %s WIB\n  Lokasi  : %s\n  Pendeta : %s",
                namaIbadah,
                tanggal != null ? tanggal.format(dateFmt) : "-",
                waktu   != null ? waktu.format(timeFmt)   : "-",
                namaLokasi != null ? namaLokasi : idLokasi,
                pendeta);
    }

    // ── Getters & Setters ────────────────────────────────────────────────
    public String getIdJadwal()  { return idJadwal; }
    public void   setIdJadwal(String v)  { this.idJadwal = v; }

    public String getNamaIbadah() { return namaIbadah; }
    public void   setNamaIbadah(String v) { this.namaIbadah = v; }

    public LocalDate getTanggal() { return tanggal; }
    public void       setTanggal(LocalDate v) { this.tanggal = v; }

    public LocalTime getWaktu() { return waktu; }
    public void       setWaktu(LocalTime v) { this.waktu = v; }

    public String getIdLokasi()   { return idLokasi; }
    public void   setIdLokasi(String v)   { this.idLokasi = v; }

    public String getNamaLokasi() { return namaLokasi; }
    public void   setNamaLokasi(String v) { this.namaLokasi = v; }

    public String getIdUser()  { return idUser; }
    public void   setIdUser(String v)  { this.idUser = v; }

    public String getNamaPendeta() { return namaPendeta; }
    public void   setNamaPendeta(String v) { this.namaPendeta = v; }

    public String getWaktuStr() {
        return waktu != null
                ? waktu.format(DateTimeFormatter.ofPattern("HH:mm")) + " WIB" : "-";
    }

    public String getTanggalStr() {
        if (tanggal == null) return "-";
        return tanggal.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }
}
