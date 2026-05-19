package com.chataja.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:chataja.db";
    private static Connection connection;

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL);
            connection.createStatement().execute("PRAGMA foreign_keys = ON;");
        }
        return connection;
    }

    public static void initialize() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // ── Tabel users ──────────────────────────────────────────────
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id_user   TEXT PRIMARY KEY,
                    nama      TEXT NOT NULL,
                    username  TEXT UNIQUE NOT NULL,
                    password  TEXT NOT NULL,
                    role      TEXT NOT NULL CHECK(role IN ('admin','majelis','jemaat'))
                );
            """);

            // ── Tabel lokasi_gereja ───────────────────────────────────────
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS lokasi_gereja (
                    id_lokasi   TEXT PRIMARY KEY,
                    nama_tempat TEXT NOT NULL,
                    alamat      TEXT,
                    kontak      TEXT,
                    id_user     TEXT,
                    FOREIGN KEY (id_user) REFERENCES users(id_user)
                );
            """);

            // ── Tabel jadwal_ibadah (dengan nama_pendeta) ─────────────────
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS jadwal_ibadah (
                    id_jadwal    TEXT PRIMARY KEY,
                    nama_ibadah  TEXT NOT NULL,
                    tanggal      DATE NOT NULL,
                    waktu        TIME NOT NULL,
                    id_lokasi    TEXT,
                    id_user      TEXT,
                    nama_pendeta TEXT,
                    FOREIGN KEY (id_lokasi) REFERENCES lokasi_gereja(id_lokasi),
                    FOREIGN KEY (id_user)   REFERENCES users(id_user)
                );
            """);

            // ── Tabel jadwal_tugas ────────────────────────────────────────
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS jadwal_tugas (
                    id_tugas      TEXT PRIMARY KEY,
                    tanggal       DATE NOT NULL,
                    tugas         TEXT NOT NULL,
                    id_user       TEXT NOT NULL,
                    waktu_ibadah  TIME,
                    FOREIGN KEY (id_user) REFERENCES users(id_user)
                );
            """);

            // ── Tabel renungan ────────────────────────────────────────────
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS renungan (
                    id_renungan TEXT PRIMARY KEY,
                    judul       TEXT NOT NULL,
                    isi         TEXT NOT NULL,
                    tanggal     DATE NOT NULL,
                    id_user     TEXT,
                    FOREIGN KEY (id_user) REFERENCES users(id_user)
                );
            """);

            // ── Tabel pengumuman ──────────────────────────────────────────
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS pengumuman (
                    id_pengumuman TEXT PRIMARY KEY,
                    judul         TEXT NOT NULL,
                    isi           TEXT NOT NULL,
                    tanggal       DATE NOT NULL,
                    id_user       TEXT,
                    foto          TEXT,
                    FOREIGN KEY (id_user) REFERENCES users(id_user)
                );
            """);

            // ── Migrasi kolom baru (aman untuk DB lama) ───────────────────
            try { stmt.execute("ALTER TABLE pengumuman ADD COLUMN foto TEXT;"); }
            catch (SQLException ignored) {}

            // Migrasi: tambah nama_pendeta jika DB sudah ada sebelumnya
            try { stmt.execute("ALTER TABLE jadwal_ibadah ADD COLUMN nama_pendeta TEXT;"); }
            catch (SQLException ignored) {}

            // Buat folder uploads jika belum ada
            new java.io.File("uploads/pengumuman").mkdirs();

            // ── Seed: akun default ────────────────────────────────────────
            stmt.execute("""
                INSERT OR IGNORE INTO users (id_user, nama, username, password, role)
                VALUES ('USR00001', 'Administrator', 'admin', 'admin123', 'admin');
            """);
            stmt.execute("""
                INSERT OR IGNORE INTO users (id_user, nama, username, password, role)
                VALUES ('USR00002', 'Rico', 'Rico', 'Rico123', 'admin');
            """);
            stmt.execute("""
                INSERT OR IGNORE INTO users (id_user, nama, username, password, role)
                VALUES ('USR00003', 'Luis', 'Luis', 'Luisep123', 'majelis');
            """);

            System.out.println("[DB] Database berhasil diinisialisasi.");

        } catch (SQLException e) {
            System.err.println("[DB] Gagal inisialisasi database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("[DB] Koneksi database ditutup.");
            }
        } catch (SQLException e) {
            System.err.println("[DB] Gagal menutup koneksi: " + e.getMessage());
        }
    }
}
