# 💬 ChatAja – Chatbot Informasi Gereja

Sistem chatbot berbasis desktop (JavaFX) untuk membantu jemaat gereja
memperoleh informasi jadwal ibadah, lokasi, renungan harian, dan pengumuman.

---

## 📋 Persyaratan Sistem

| Kebutuhan | Versi Minimum |
|-----------|--------------|
| Java (JDK) | 17 atau lebih baru |
| Maven | 3.8+ |
| OS | Windows 7–11 / Linux / macOS |

---

## 🚀 Cara Menjalankan

### Windows
```bat
run.bat
```

### Linux / macOS
```bash
chmod +x run.sh
./run.sh
```

### Manual
```bash
mvn clean javafx:run
```

### Build JAR
```bash
mvn clean package
java --module-path <path-to-javafx-sdk>/lib \
     --add-modules javafx.controls,javafx.fxml \
     -jar target/chataja-1.0.jar
```

---

## 👥 Akun Default

| Role  | Username | Password |
|-------|----------|----------|
| Admin | admin    | admin123 |

> Akun Majelis perlu ditambahkan sendiri via database atau
> tambahkan langsung ke tabel `users` dengan role = `'majelis'`.

---

## 🏗️ Struktur Proyek

```
chataja/
├── pom.xml                          # Maven build file
├── run.bat / run.sh                 # Script jalankan aplikasi
├── chataja.db                       # SQLite DB (dibuat otomatis saat pertama run)
└── src/main/java/com/chataja/
    ├── Main.java                    # Entry point
    ├── model/
    │   ├── User.java                # Base class pengguna
    │   ├── Admin.java               # Model Admin
    │   ├── Majelis.java             # Model Majelis
    │   ├── JadwalIbadah.java        # Model Jadwal Ibadah
    │   ├── JadwalTugas.java         # Model Jadwal Tugas
    │   ├── Lokasi.java              # Model Lokasi Gereja
    │   ├── Renungan.java            # Model Renungan Harian
    │   └── Pengumuman.java          # Model Pengumuman
    ├── db/
    │   └── DatabaseManager.java     # Manajemen koneksi SQLite
    ├── dao/
    │   ├── UserDAO.java             # CRUD Users
    │   ├── JadwalIbadahDAO.java     # CRUD Jadwal Ibadah
    │   ├── JadwalTugasDAO.java      # CRUD Jadwal Tugas
    │   ├── LokasiDAO.java           # CRUD Lokasi Gereja
    │   ├── RenunganDAO.java         # CRUD Renungan
    │   └── PengumumanDAO.java       # CRUD Pengumuman
    ├── chatbot/
    │   └── ChatBot.java             # Engine chatbot (intent matching)
    └── ui/
        ├── ChatAjaApp.java          # Main JavaFX Application
        ├── ChatView.java            # Tampilan chat utama
        ├── LoginDialog.java         # Dialog login
        ├── AdminView.java           # Panel admin (Jadwal Ibadah, Tugas, Lokasi)
        └── MajelisView.java         # Panel majelis (Pengumuman, Renungan)
```

---

## 🔑 Use Cases yang Diimplementasi

| UC   | Deskripsi | Aktor |
|------|-----------|-------|
| UC-1 | Melihat jadwal ibadah via chatbot | Jemaat |
| UC-2 | Melihat lokasi & kontak gereja | Jemaat |
| UC-3 | Kelola data master (CRUD) | Admin |
| UC-4 | Melihat jadwal tugas pelayanan | Majelis |
| UC-5 | Kelola pengumuman & renungan | Majelis |
| UC-6 | Membaca renungan harian | Jemaat |
| UC-7 | Kata sambutan otomatis saat buka app | Jemaat |
| UC-8 | Membaca pengumuman gereja | Jemaat |

---

## 🗄️ Skema Database (SQLite)

```sql
users            (id_user, nama, username, password, role)
lokasi_gereja    (id_lokasi, nama_tempat, alamat, kontak, id_user)
jadwal_ibadah    (id_jadwal, nama_ibadah, tanggal, waktu, id_lokasi, id_user)
jadwal_tugas     (id_tugas, tanggal, tugas, id_user, waktu_ibadah)
renungan         (id_renungan, judul, isi, tanggal, id_user)
pengumuman       (id_pengumuman, judul, isi, tanggal, id_user)
```

---

## 💡 Saran Pengembangan Lanjutan

Beberapa hal yang bisa ditingkatkan:

1. **Manajemen User oleh Admin** – Tambahkan tab di AdminView untuk membuat,
   mengedit, dan menghapus akun Majelis.

2. **NLP yang lebih baik** – Ganti keyword matching dengan model NLP sederhana
   (Naive Bayes / TF-IDF) agar chatbot lebih fleksibel memahami variasi pertanyaan.

3. **Export laporan** – Ekspor jadwal ibadah/tugas ke format PDF atau Excel.

4. **Notifikasi jadwal** – Tampilkan reminder jadwal ibadah/tugas yang akan datang
   dalam beberapa hari ke depan.

5. **Foto/lampiran pengumuman** – Dukung upload gambar pada pengumuman.

6. **Backup & restore database** – Fitur admin untuk backup file chataja.db.

---

## 📞 Informasi Proyek

- **Mata Kuliah**: Rekayasa Perangkat Lunak Berorientasi Objek (TI0373)
- **Semester**: Genap TA 2025/2026
- **Program Studi**: Informatika – FTI UKDW

**Tim Pengembang:**
1. Philip Luis Nurcahyo | 71241095
2. Hervian Paskah Pradana | 71241107
3. Hendrikus Lanang Ona | 71241114
4. Putra Eka Setiawan | 71241116
