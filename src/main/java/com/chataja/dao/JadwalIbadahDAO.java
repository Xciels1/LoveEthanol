package com.chataja.dao;

import com.chataja.db.DatabaseManager;
import com.chataja.model.JadwalIbadah;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object untuk tabel jadwal_ibadah.
 * Update: support kolom nama_pendeta.
 */
public class JadwalIbadahDAO {

    /** Ambil semua jadwal ibadah beserta nama lokasi (JOIN) */
    public List<JadwalIbadah> getAll() {
        List<JadwalIbadah> list = new ArrayList<>();
        String sql = """
            SELECT j.*, l.nama_tempat
            FROM jadwal_ibadah j
            LEFT JOIN lokasi_gereja l ON j.id_lokasi = l.id_lokasi
            ORDER BY j.tanggal, j.waktu
        """;
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            System.err.println("[JadwalIbadahDAO] getAll error: " + e.getMessage());
        }
        return list;
    }

    /** Ambil jadwal ibadah yang akan datang (untuk chatbot) */
    public List<JadwalIbadah> getUpcoming() {
        List<JadwalIbadah> list = new ArrayList<>();
        String today = LocalDate.now().toString();
        String sql = """
            SELECT j.*, l.nama_tempat
            FROM jadwal_ibadah j
            LEFT JOIN lokasi_gereja l ON j.id_lokasi = l.id_lokasi
            WHERE j.tanggal >= ?
            ORDER BY j.tanggal, j.waktu
            LIMIT 10
        """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, today);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            System.err.println("[JadwalIbadahDAO] getUpcoming error: " + e.getMessage());
        }
        return list;
    }

    /**
     * Ambil jadwal ibadah untuk minggu ini saja
     * (dari hari ini sampai hari Minggu terdekat).
     * Jika hari ini sudah Minggu, tampilkan jadwal hari ini saja.
     */
    public List<JadwalIbadah> getThisWeek() {
        List<JadwalIbadah> list = new ArrayList<>();
        LocalDate today = LocalDate.now();

        // Hitung hari Minggu terdekat (akhir minggu)
        java.time.DayOfWeek todayDow = today.getDayOfWeek();
        int daysUntilSunday = (java.time.DayOfWeek.SUNDAY.getValue() - todayDow.getValue() + 7) % 7;
        // Jika hari ini Minggu, daysUntilSunday = 0 → tampilkan hari ini
        LocalDate endOfWeek = today.plusDays(daysUntilSunday);

        String sql = """
            SELECT j.*, l.nama_tempat
            FROM jadwal_ibadah j
            LEFT JOIN lokasi_gereja l ON j.id_lokasi = l.id_lokasi
            WHERE j.tanggal >= ? AND j.tanggal <= ?
            ORDER BY j.tanggal, j.waktu
        """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, today.toString());
            ps.setString(2, endOfWeek.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            System.err.println("[JadwalIbadahDAO] getThisWeek error: " + e.getMessage());
        }
        return list;
    }

    public boolean insert(JadwalIbadah j) {
        String sql = """
            INSERT INTO jadwal_ibadah
                (id_jadwal, nama_ibadah, tanggal, waktu, id_lokasi, id_user, nama_pendeta)
            VALUES (?,?,?,?,?,?,?)
        """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, generateId());
            ps.setString(2, j.getNamaIbadah());
            ps.setString(3, j.getTanggal() != null ? j.getTanggal().toString() : null);
            ps.setString(4, j.getWaktu()   != null ? j.getWaktu().toString()   : null);
            ps.setString(5, j.getIdLokasi());
            ps.setString(6, j.getIdUser());
            ps.setString(7, j.getNamaPendeta());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[JadwalIbadahDAO] insert error: " + e.getMessage());
            return false;
        }
    }

    public boolean update(JadwalIbadah j) {
        String sql = """
            UPDATE jadwal_ibadah
            SET nama_ibadah=?, tanggal=?, waktu=?, id_lokasi=?, nama_pendeta=?
            WHERE id_jadwal=?
        """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, j.getNamaIbadah());
            ps.setString(2, j.getTanggal() != null ? j.getTanggal().toString() : null);
            ps.setString(3, j.getWaktu()   != null ? j.getWaktu().toString()   : null);
            ps.setString(4, j.getIdLokasi());
            ps.setString(5, j.getNamaPendeta());
            ps.setString(6, j.getIdJadwal());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[JadwalIbadahDAO] update error: " + e.getMessage());
            return false;
        }
    }

    public boolean delete(String id) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "DELETE FROM jadwal_ibadah WHERE id_jadwal=?")) {
            ps.setString(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[JadwalIbadahDAO] delete error: " + e.getMessage());
            return false;
        }
    }

    private JadwalIbadah map(ResultSet rs) throws SQLException {
        JadwalIbadah j = new JadwalIbadah();
        j.setIdJadwal(rs.getString("id_jadwal"));
        j.setNamaIbadah(rs.getString("nama_ibadah"));
        String tgl = rs.getString("tanggal");
        if (tgl != null) j.setTanggal(LocalDate.parse(tgl));
        String wkt = rs.getString("waktu");
        if (wkt != null) j.setWaktu(LocalTime.parse(wkt.length() == 5 ? wkt : wkt.substring(0, 5)));
        j.setIdLokasi(rs.getString("id_lokasi"));
        j.setIdUser(rs.getString("id_user"));
        try { j.setNamaLokasi(rs.getString("nama_tempat")); }  catch (Exception ignored) {}
        try { j.setNamaPendeta(rs.getString("nama_pendeta")); } catch (Exception ignored) {}
        return j;
    }

    private String generateId() {
        return "JDW" + String.format("%05d", System.currentTimeMillis() % 100000);
    }
}
