package com.chataja.model;

/**
 * Kelas dasar untuk semua pengguna sistem ChatAja.
 * Sesuai class diagram SRS: User memiliki id, Nama, Username, Password.
 */
public abstract class User {
    private String idUser;
    private String nama;
    private String username;
    private String password;
    private String role; // "admin", "majelis", "jemaat"

    public User(String idUser, String nama, String username, String password, String role) {
        this.idUser = idUser;
        this.nama = nama;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public User() {}

    // ===================== Abstract Methods =====================
    public abstract void login();
    public abstract void logout();

    // ===================== Getters & Setters =====================
    public String getIdUser() { return idUser; }
    public void setIdUser(String idUser) { this.idUser = idUser; }

    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    @Override
    public String toString() {
        return "User{id='" + idUser + "', nama='" + nama + "', role='" + role + "'}";
    }
}
