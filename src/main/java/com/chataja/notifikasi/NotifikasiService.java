package com.chataja.notifikasi;

import com.chataja.dao.JadwalIbadahDAO;
import com.chataja.dao.JadwalTugasDAO;
import com.chataja.model.JadwalIbadah;
import com.chataja.model.JadwalTugas;
import com.chataja.model.User;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

/**
 * NotifikasiService – cek jadwal mendatang dan tampilkan popup.
 *
 * Dipanggil setelah login berhasil (semua role) dan saat startup
 * tanpa login (jemaat umum, hanya jadwal ibadah).
 *
 * Logika:
 *  • Semua role  → tampilkan Jadwal Ibadah dalam HARI_KEDEPAN hari ke depan.
 *  • Role majelis → tambahkan Jadwal Tugas mereka sendiri dalam rentang yang sama.
 */
public class NotifikasiService {

    /** Jangkauan notifikasi: hari ini + 3 hari ke depan */
    public static final int HARI_KEDEPAN = 3;

    private final JadwalIbadahDAO jadwalIbadahDAO = new JadwalIbadahDAO();
    private final JadwalTugasDAO  jadwalTugasDAO  = new JadwalTugasDAO();

    // ── Public API ────────────────────────────────────────────────────

    /**
     * Cek jadwal dan tampilkan popup jika ada notifikasi.
     * Gunakan user = null untuk jemaat yang belum login.
     */
    public void cekDanTampilkan(User user) {
        LocalDate today = LocalDate.now();
        LocalDate batas = today.plusDays(HARI_KEDEPAN);

        List<JadwalIbadah> jadwalIbadah = jadwalIbadahDAO.getInRange(today, batas);

        List<JadwalTugas> jadwalTugas = Collections.emptyList();
        if (user != null && "majelis".equals(user.getRole())) {
            jadwalTugas = jadwalTugasDAO.getByUserInRange(user.getIdUser(), today, batas);
        }

        boolean adaNotif = !jadwalIbadah.isEmpty() || !jadwalTugas.isEmpty();
        if (adaNotif) {
            String sapaan = (user != null) ? user.getNama() : null;
            new NotifikasiPopup(jadwalIbadah, jadwalTugas, today, sapaan).tampilkan();
        }
    }

    /**
     * Versi tanpa login – hanya jadwal ibadah.
     */
    public void cekDanTampilkanTanpaLogin() {
        cekDanTampilkan(null);
    }
}
