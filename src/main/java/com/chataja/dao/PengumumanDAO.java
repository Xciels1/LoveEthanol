package com.chataja.dao;

import com.chataja.db.DatabaseManager;
import com.chataja.model.Pengumuman;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object untuk tabel pengumuman.
 */
public class PengumumanDAO {

    /** Ambil semua pengumuman, terbaru lebih dulu */
    public List<Pengumuman> getAll() {
        List<Pengumuman> list = new ArrayList<>();
        String sql = "SELECT * FROM pengumuman ORDER BY tanggal DESC";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            System.err.println("[PengumumanDAO] getAll error: " + e.getMessage());
        }
        return list;
    }

    /** Ambil pengumuman terbaru (untuk chatbot) */
    public List<Pengumuman> getLatest(int limit) {
        List<Pengumuman> list = new ArrayList<>();
        String sql = "SELECT * FROM pengumuman ORDER BY tanggal DESC LIMIT ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            System.err.println("[PengumumanDAO] getLatest error: " + e.getMessage());
        }
        return list;
    }

//    public boolean insert(Pengumuman p) {
//        String sql = "INSERT INTO pengumuman (id_pengumuman,judul,isi,tanggal,id_user) VALUES (?,?,?,?,?)";
//        try (Connection conn = DatabaseManager.getConnection();
//             PreparedStatement ps = conn.prepareStatement(sql)) {
//            ps.setString(1, generateId());
//            ps.setString(2, p.getJudul());
//            ps.setString(3, p.getIsi());
//            ps.setString(4, p.getTanggal() != null ? p.getTanggal().toString() : null);
//            ps.setString(5, p.getIdUser());
//            return ps.executeUpdate() > 0;
//        } catch (SQLException e) {
//            System.err.println("[PengumumanDAO] insert error: " + e.getMessage());
//            return false;
//        }
//    }

    public boolean insert(Pengumuman p) {
        String sql = "INSERT INTO pengumuman (id_pengumuman,judul,isi,tanggal,id_user,foto) VALUES (?,?,?,?,?,?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, generateId());
            ps.setString(2, p.getJudul());
            ps.setString(3, p.getIsi());
            ps.setString(4, p.getTanggal() != null ? p.getTanggal().toString() : null);
            ps.setString(5, p.getIdUser());
            ps.setString(6, p.getFotoPath());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[PengumumanDAO] insert error: " + e.getMessage());
            return false;
        }
    }

//    public boolean update(Pengumuman p) {
//        String sql = "UPDATE pengumuman SET judul=?,isi=?,tanggal=? WHERE id_pengumuman=?";
//        try (Connection conn = DatabaseManager.getConnection();
//             PreparedStatement ps = conn.prepareStatement(sql)) {
//            ps.setString(1, p.getJudul());
//            ps.setString(2, p.getIsi());
//            ps.setString(3, p.getTanggal() != null ? p.getTanggal().toString() : null);
//            ps.setString(4, p.getIdPengumuman());
//            return ps.executeUpdate() > 0;
//        } catch (SQLException e) {
//            System.err.println("[PengumumanDAO] update error: " + e.getMessage());
//            return false;
//        }
//    }

    public boolean update(Pengumuman p) {
        String sql = "UPDATE pengumuman SET judul=?,isi=?,tanggal=?,foto=? WHERE id_pengumuman=?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getJudul());
            ps.setString(2, p.getIsi());
            ps.setString(3, p.getTanggal() != null ? p.getTanggal().toString() : null);
            ps.setString(4, p.getFotoPath());
            ps.setString(5, p.getIdPengumuman());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[PengumumanDAO] update error: " + e.getMessage());
            return false;
        }
    }

    public boolean delete(String id) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM pengumuman WHERE id_pengumuman=?")) {
            ps.setString(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[PengumumanDAO] delete error: " + e.getMessage());
            return false;
        }
    }

    private Pengumuman map(ResultSet rs) throws SQLException {
        Pengumuman p = new Pengumuman();
        p.setIdPengumuman(rs.getString("id_pengumuman"));
        p.setJudul(rs.getString("judul"));
        p.setIsi(rs.getString("isi"));
        String tgl = rs.getString("tanggal");
        if (tgl != null) p.setTanggal(LocalDate.parse(tgl));
        p.setIdUser(rs.getString("id_user"));
        p.setFotoPath(rs.getString("foto"));
        return p;
    }

    private String generateId() {
        return "PGM" + String.format("%05d", System.currentTimeMillis() % 100000);
    }
}
