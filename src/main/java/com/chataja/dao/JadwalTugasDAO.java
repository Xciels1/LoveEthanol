package com.chataja.dao;

import com.chataja.db.DatabaseManager;
import com.chataja.model.JadwalTugas;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object untuk tabel jadwal_tugas.
 */
public class JadwalTugasDAO {

    /** Ambil semua jadwal tugas majelis (untuk Admin view) */
    public List<JadwalTugas> getAll() {
        List<JadwalTugas> list = new ArrayList<>();
        String sql = """
            SELECT jt.*, u.nama as nama_majelis
            FROM jadwal_tugas jt
            LEFT JOIN users u ON jt.id_user = u.id_user
            ORDER BY jt.tanggal
        """;
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            System.err.println("[JadwalTugasDAO] getAll error: " + e.getMessage());
        }
        return list;
    }

    /** Ambil jadwal tugas untuk majelis tertentu (untuk UC-4) */
    public List<JadwalTugas> getByMajelis(String idUser) {
        List<JadwalTugas> list = new ArrayList<>();
        String today = LocalDate.now().toString();
        String sql = """
            SELECT jt.*, u.nama as nama_majelis
            FROM jadwal_tugas jt
            LEFT JOIN users u ON jt.id_user = u.id_user
            WHERE jt.id_user = ? AND jt.tanggal >= ?
            ORDER BY jt.tanggal
        """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, idUser);
            ps.setString(2, today);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            System.err.println("[JadwalTugasDAO] getByMajelis error: " + e.getMessage());
        }
        return list;
    }

    public boolean insert(JadwalTugas jt) {
        String sql = "INSERT INTO jadwal_tugas (id_tugas,tanggal,tugas,id_user,waktu_ibadah) VALUES (?,?,?,?,?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, generateId());
            ps.setString(2, jt.getTanggal() != null ? jt.getTanggal().toString() : null);
            ps.setString(3, jt.getTugas());
            ps.setString(4, jt.getIdUser());
            ps.setString(5, jt.getWaktuIbadah() != null ? jt.getWaktuIbadah().toString() : null);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[JadwalTugasDAO] insert error: " + e.getMessage());
            return false;
        }
    }

    public boolean update(JadwalTugas jt) {
        String sql = "UPDATE jadwal_tugas SET tanggal=?,tugas=?,id_user=?,waktu_ibadah=? WHERE id_tugas=?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, jt.getTanggal() != null ? jt.getTanggal().toString() : null);
            ps.setString(2, jt.getTugas());
            ps.setString(3, jt.getIdUser());
            ps.setString(4, jt.getWaktuIbadah() != null ? jt.getWaktuIbadah().toString() : null);
            ps.setString(5, jt.getIdTugas());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[JadwalTugasDAO] update error: " + e.getMessage());
            return false;
        }
    }

    public boolean delete(String id) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM jadwal_tugas WHERE id_tugas=?")) {
            ps.setString(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[JadwalTugasDAO] delete error: " + e.getMessage());
            return false;
        }
    }

    private JadwalTugas map(ResultSet rs) throws SQLException {
        JadwalTugas jt = new JadwalTugas();
        jt.setIdTugas(rs.getString("id_tugas"));
        String tgl = rs.getString("tanggal");
        if (tgl != null) jt.setTanggal(LocalDate.parse(tgl));
        jt.setTugas(rs.getString("tugas"));
        jt.setIdUser(rs.getString("id_user"));
        try { jt.setNamaMajelis(rs.getString("nama_majelis")); } catch (Exception ignored) {}
        String wkt = rs.getString("waktu_ibadah");
        if (wkt != null && !wkt.isEmpty()) {
            try { jt.setWaktuIbadah(LocalTime.parse(wkt.length() >= 5 ? wkt.substring(0,5) : wkt)); }
            catch (Exception ignored) {}
        }
        return jt;
    }

    private String generateId() {
        return "TGS" + String.format("%05d", System.currentTimeMillis() % 100000);
    }
}
