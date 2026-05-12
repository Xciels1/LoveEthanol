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

    /** Ambil jadwal ibadah untuk tanggal terdekat (minggu ini) */
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

    public boolean insert(JadwalIbadah j) {
        String sql = "INSERT INTO jadwal_ibadah (id_jadwal,nama_ibadah,tanggal,waktu,id_lokasi,id_user) VALUES (?,?,?,?,?,?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, generateId());
            ps.setString(2, j.getNamaIbadah());
            ps.setString(3, j.getTanggal() != null ? j.getTanggal().toString() : null);
            ps.setString(4, j.getWaktu() != null ? j.getWaktu().toString() : null);
            ps.setString(5, j.getIdLokasi());
            ps.setString(6, j.getIdUser());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[JadwalIbadahDAO] insert error: " + e.getMessage());
            return false;
        }
    }

    public boolean update(JadwalIbadah j) {
        String sql = "UPDATE jadwal_ibadah SET nama_ibadah=?,tanggal=?,waktu=?,id_lokasi=? WHERE id_jadwal=?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, j.getNamaIbadah());
            ps.setString(2, j.getTanggal() != null ? j.getTanggal().toString() : null);
            ps.setString(3, j.getWaktu() != null ? j.getWaktu().toString() : null);
            ps.setString(4, j.getIdLokasi());
            ps.setString(5, j.getIdJadwal());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[JadwalIbadahDAO] update error: " + e.getMessage());
            return false;
        }
    }

    public boolean delete(String id) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM jadwal_ibadah WHERE id_jadwal=?")) {
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
        try { j.setNamaLokasi(rs.getString("nama_tempat")); } catch (Exception ignored) {}
        return j;
    }

    private String generateId() {
        return "JDW" + String.format("%05d", System.currentTimeMillis() % 100000);
    }
}
