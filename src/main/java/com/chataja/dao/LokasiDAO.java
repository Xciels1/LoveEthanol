package com.chataja.dao;

import com.chataja.db.DatabaseManager;
import com.chataja.model.Lokasi;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object untuk tabel lokasi_gereja.
 */
public class LokasiDAO {

    public List<Lokasi> getAll() {
        List<Lokasi> list = new ArrayList<>();
        String sql = "SELECT * FROM lokasi_gereja ORDER BY nama_tempat";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            System.err.println("[LokasiDAO] getAll error: " + e.getMessage());
        }
        return list;
    }

    public Lokasi getById(String idLokasi) {
        String sql = "SELECT * FROM lokasi_gereja WHERE id_lokasi=?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, idLokasi);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return map(rs);
        } catch (SQLException e) {
            System.err.println("[LokasiDAO] getById error: " + e.getMessage());
        }
        return null;
    }

    public boolean insert(Lokasi l) {
        String sql = "INSERT INTO lokasi_gereja (id_lokasi,nama_tempat,alamat,kontak,id_user) VALUES (?,?,?,?,?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, generateId());
            ps.setString(2, l.getNamaTempat());
            ps.setString(3, l.getAlamat());
            ps.setString(4, l.getKontak());
            ps.setString(5, l.getIdUser());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[LokasiDAO] insert error: " + e.getMessage());
            return false;
        }
    }

    public boolean update(Lokasi l) {
        String sql = "UPDATE lokasi_gereja SET nama_tempat=?,alamat=?,kontak=? WHERE id_lokasi=?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, l.getNamaTempat());
            ps.setString(2, l.getAlamat());
            ps.setString(3, l.getKontak());
            ps.setString(4, l.getIdLokasi());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[LokasiDAO] update error: " + e.getMessage());
            return false;
        }
    }

    public boolean delete(String id) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM lokasi_gereja WHERE id_lokasi=?")) {
            ps.setString(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[LokasiDAO] delete error: " + e.getMessage());
            return false;
        }
    }

    private Lokasi map(ResultSet rs) throws SQLException {
        return new Lokasi(
                rs.getString("id_lokasi"),
                rs.getString("nama_tempat"),
                rs.getString("alamat"),
                rs.getString("kontak"),
                rs.getString("id_user")
        );
    }

    private String generateId() {
        return "LOK" + String.format("%05d", System.currentTimeMillis() % 100000);
    }
}
