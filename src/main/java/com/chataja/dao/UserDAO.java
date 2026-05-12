package com.chataja.dao;

import com.chataja.db.DatabaseManager;
import com.chataja.model.Admin;
import com.chataja.model.Majelis;
import com.chataja.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object untuk tabel users.
 */
public class UserDAO {

    /**
     * Autentikasi login berdasarkan username, password, dan role.
     * @return User object jika berhasil, null jika gagal.
     */
    public User login(String username, String password, String role) {
        String sql = "SELECT * FROM users WHERE username=? AND password=? AND role=?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, role);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapUser(rs);
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] login error: " + e.getMessage());
        }
        return null;
    }

    /** Mengambil semua user dengan role majelis */
    public List<User> getAllMajelis() {
        return getUsersByRole("majelis");
    }

    /** Mengambil semua user berdasarkan role */
    public List<User> getUsersByRole(String role) {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE role=? ORDER BY nama";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, role);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapUser(rs));
        } catch (SQLException e) {
            System.err.println("[UserDAO] getUsersByRole error: " + e.getMessage());
        }
        return list;
    }

    /** Tambah user baru (majelis) */
    public boolean insert(User user) {
        String sql = "INSERT INTO users (id_user,nama,username,password,role) VALUES (?,?,?,?,?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, generateId());
            ps.setString(2, user.getNama());
            ps.setString(3, user.getUsername());
            ps.setString(4, user.getPassword());
            ps.setString(5, user.getRole());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[UserDAO] insert error: " + e.getMessage());
            return false;
        }
    }

    /** Update user */
    public boolean update(User user) {
        String sql = "UPDATE users SET nama=?,username=?,password=? WHERE id_user=?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getNama());
            ps.setString(2, user.getUsername());
            ps.setString(3, user.getPassword());
            ps.setString(4, user.getIdUser());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[UserDAO] update error: " + e.getMessage());
            return false;
        }
    }

    /** Hapus user berdasarkan ID */
    public boolean delete(String idUser) {
        String sql = "DELETE FROM users WHERE id_user=?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, idUser);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[UserDAO] delete error: " + e.getMessage());
            return false;
        }
    }

    // ── Private helpers ────────────────────────────────────────────────────

    private User mapUser(ResultSet rs) throws SQLException {
        String role = rs.getString("role");
        User u;
        if ("admin".equals(role)) {
            u = new Admin();
        } else if ("majelis".equals(role)) {
            u = new Majelis();
        } else {
            u = new Admin(); // fallback – treat unknown as admin object
        }
        u.setIdUser(rs.getString("id_user"));
        u.setNama(rs.getString("nama"));
        u.setUsername(rs.getString("username"));
        u.setPassword(rs.getString("password"));
        u.setRole(role);
        return u;
    }

    private String generateId() {
        return "USR" + String.format("%05d", System.currentTimeMillis() % 100000);
    }
}
