package com.chataja.dao;

import com.chataja.db.DatabaseManager;
import com.chataja.model.Renungan;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object untuk tabel renungan.
 */
public class RenunganDAO {

    /** Ambil renungan untuk hari ini. Jika tidak ada, ambil yang terbaru. */
    public Renungan getHariIni() {
        String today = LocalDate.now().toString();
        // Coba renungan hari ini
        String sql = "SELECT * FROM renungan WHERE tanggal = ? LIMIT 1";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, today);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return map(rs);
        } catch (SQLException e) {
            System.err.println("[RenunganDAO] getHariIni error: " + e.getMessage());
        }

        // Fallback: renungan sebelumnya (terbaru)
        String fallback = "SELECT * FROM renungan ORDER BY tanggal DESC LIMIT 1";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(fallback)) {
            if (rs.next()) return map(rs);
        } catch (SQLException e) {
            System.err.println("[RenunganDAO] fallback error: " + e.getMessage());
        }
        return null;
    }

    public List<Renungan> getAll() {
        List<Renungan> list = new ArrayList<>();
        String sql = "SELECT * FROM renungan ORDER BY tanggal DESC";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            System.err.println("[RenunganDAO] getAll error: " + e.getMessage());
        }
        return list;
    }

    public boolean insert(Renungan r) {
        String sql = "INSERT INTO renungan (id_renungan,judul,isi,tanggal,id_user) VALUES (?,?,?,?,?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, generateId());
            ps.setString(2, r.getJudul());
            ps.setString(3, r.getIsi());
            ps.setString(4, r.getTanggal() != null ? r.getTanggal().toString() : null);
            ps.setString(5, r.getIdUser());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[RenunganDAO] insert error: " + e.getMessage());
            return false;
        }
    }

    public boolean update(Renungan r) {
        String sql = "UPDATE renungan SET judul=?,isi=?,tanggal=? WHERE id_renungan=?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, r.getJudul());
            ps.setString(2, r.getIsi());
            ps.setString(3, r.getTanggal() != null ? r.getTanggal().toString() : null);
            ps.setString(4, r.getIdRenungan());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[RenunganDAO] update error: " + e.getMessage());
            return false;
        }
    }

    public boolean delete(String id) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM renungan WHERE id_renungan=?")) {
            ps.setString(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[RenunganDAO] delete error: " + e.getMessage());
            return false;
        }
    }

    private Renungan map(ResultSet rs) throws SQLException {
        Renungan r = new Renungan();
        r.setIdRenungan(rs.getString("id_renungan"));
        r.setJudul(rs.getString("judul"));
        r.setIsi(rs.getString("isi"));
        String tgl = rs.getString("tanggal");
        if (tgl != null) r.setTanggal(LocalDate.parse(tgl));
        r.setIdUser(rs.getString("id_user"));
        return r;
    }

    private String generateId() {
        return "REN" + String.format("%05d", System.currentTimeMillis() % 100000);
    }
}
