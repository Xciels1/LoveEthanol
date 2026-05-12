package com.chataja.model;

/**
 * Kelas Admin – dapat mengelola semua data master sistem.
 * Aksi: tambahData, hapusData, editData (CRUD data jadwal ibadah & tugas).
 */
public class Admin extends User {

    public Admin(String idUser, String nama, String username, String password) {
        super(idUser, nama, username, password, "admin");
    }

    public Admin() {
        super();
        setRole("admin");
    }

    @Override
    public void login() {
        System.out.println("[Admin] " + getNama() + " berhasil login.");
    }

    @Override
    public void logout() {
        System.out.println("[Admin] " + getNama() + " logout.");
    }

    /** Operasi tambah data ke sistem */
    public void tambahData(String jenis) {
        System.out.println("[Admin] Menambahkan data: " + jenis);
    }

    /** Operasi hapus data dari sistem */
    public void hapusData(String jenis, String id) {
        System.out.println("[Admin] Menghapus data " + jenis + " id=" + id);
    }

    /** Operasi edit data di sistem */
    public void editData(String jenis, String id) {
        System.out.println("[Admin] Mengedit data " + jenis + " id=" + id);
    }
}
