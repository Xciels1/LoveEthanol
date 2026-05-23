package com.chataja.chatbot;

import com.chataja.dao.*;
import com.chataja.model.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatBot {

    private final String namaBot = "ChatAja";
    private User loggedUser;

    // ── DAO ──────────────────────────────────────────────────────────────
    private final JadwalIbadahDAO jadwalIbadahDAO = new JadwalIbadahDAO();
    private final LokasiDAO       lokasiDAO       = new LokasiDAO();
    private final RenunganDAO     renunganDAO     = new RenunganDAO();
    private final PengumumanDAO   pengumumanDAO   = new PengumumanDAO();
    private final JadwalTugasDAO  jadwalTugasDAO  = new JadwalTugasDAO();
    private final AyatAlkitabDAO  ayatDAO         = new AyatAlkitabDAO();

    // ── Enum Intent ───────────────────────────────────────────────────────
    private enum Intent {
        JADWAL_IBADAH, JADWAL_IBADAH_MINGGU_INI, LOKASI, RENUNGAN,
        PENGUMUMAN, KONTAK, JADWAL_TUGAS, SAPAAN, BANTUAN,
        TIDAK_DIKENALI, AYAT_ALKITAB, DOA, TENTANG_BOT, UCAPAN_BERKAT
    }

    public ChatBot() {}
    public ChatBot(User loggedUser) { this.loggedUser = loggedUser; }

    public void setLoggedUser(User user) { this.loggedUser = user; }
    public User getLoggedUser()          { return loggedUser; }
    public String getNamaBot()           { return namaBot; }

    // ── Public accessor untuk ChatView ────────────────────────────────────
    public Renungan getRenunganHariIni() {
        return renunganDAO.getHariIni();
    }

    public List<Pengumuman> getLatestPengumuman(int limit) {
        return pengumumanDAO.getLatest(limit);
    }

    // ────────────────────────────────────────────────────────────────────
    //  PUBLIC INTERFACE
    // ────────────────────────────────────────────────────────────────────

    /**
     * Proses pertanyaan utama.
     * Otomatis mendeteksi apakah pertanyaan mengandung multi-intent.
     * Jika ya → gabungkan semua jawaban.
     * Jika tidak → jawab seperti biasa.
     */
    public String prosesPertanyaan(String input) {
        if (!validasiInput(input)) {
            return " Mohon masukkan pertanyaan yang valid (tidak boleh kosong).";
        }

        String lower = input.toLowerCase().trim();

        List<Intent> semuaIntent = deteksiSemuaIntent(lower);

        // Hanya 1 intent → jawab normal
        if (semuaIntent.size() <= 1) {
            Intent intent = semuaIntent.isEmpty()
                    ? deteksiIntent(lower)
                    : semuaIntent.get(0);
            return displayJawaban(intent, input);
        }

        // Lebih dari 1 → gabungkan semua jawaban
        return prosesMultiIntent(semuaIntent, input);
    }

    public boolean validasiInput(String input) {
        return input != null && !input.trim().isEmpty();
    }

    public String displayJawaban(Intent intent, String input) {
        return switch (intent) {
            case JADWAL_IBADAH             -> responJadwalIbadah();
            case JADWAL_IBADAH_MINGGU_INI  -> responJadwalIbadahMingguIni();
            case LOKASI        -> responLokasi();
            case RENUNGAN      -> responRenungan();
            case PENGUMUMAN    -> responPengumuman();
            case KONTAK        -> responKontak();
            case JADWAL_TUGAS  -> responJadwalTugas();
            case SAPAAN        -> responSapaan();
            case BANTUAN       -> responBantuan();
            case AYAT_ALKITAB  -> responAyatAlkitab(input);
            case DOA           -> responDoa();
            case TENTANG_BOT   -> responTentangBot();
            case UCAPAN_BERKAT -> responUcapanBerkat();
            default            -> responTidakDikenali(input);
        };
    }

    // ────────────────────────────────────────────────────────────────────
    //  MULTI-INTENT DETECTION
    // ────────────────────────────────────────────────────────────────────

    /**
     * Deteksi SEMUA intent yang ada dalam satu kalimat input.
     * Berbeda dengan deteksiIntent() yang berhenti di match pertama,
     * method ini terus mengecek semua kemungkinan intent.
     *
     * Hanya intent "informasional" yang bisa digabung:
     *   JADWAL_IBADAH, LOKASI, RENUNGAN, PENGUMUMAN,
     *   KONTAK, AYAT_ALKITAB, JADWAL_TUGAS
     *
     * Intent standalone (tidak digabung):
     *   SAPAAN, BANTUAN, TENTANG_BOT, UCAPAN_BERKAT, DOA
     */
    private List<Intent> deteksiSemuaIntent(String text) {
        List<Intent> intents = new ArrayList<>();

        // ── Standalone — langsung return 1 saja ──────────────────────────
        if (containsAny(text, "kamu itu apa", "siapa kamu", "siapa chataja",
                "apa itu chataja", "tentang chataja", "tentang bot")) {
            intents.add(Intent.TENTANG_BOT); return intents;
        }
        if (containsAny(text, "tuhan memberkati", "gbu", "god bless",
                "berkat tuhan", "kiranya tuhan", "shalom")) {
            intents.add(Intent.UCAPAN_BERKAT); return intents;
        }
        if (containsAny(text, "halo", "hai", "hi", "hello", "selamat pagi",
                "selamat siang", "selamat malam", "selamat sore", "hei")) {
            intents.add(Intent.SAPAAN); return intents;
        }
        if (containsAny(text, "bantuan", "help", "menu", "bisa apa",
                "apa saja", "fitur", "panduan", "cara pakai")) {
            intents.add(Intent.BANTUAN); return intents;
        }
        if (containsAny(text, "doa", "berdoa", "doakan",
                "minta doa", "tolong doakan", "mohon doa")) {
            intents.add(Intent.DOA); return intents;
        }

        // ── Informasional — semua yang cocok dikumpulkan ──────────────────

        // Cek "minggu ini" lebih dulu (lebih spesifik)
        if (containsAny(text, "ibadah minggu ini", "jadwal ibadah minggu ini",
                "jadwal minggu ini", "kebaktian minggu ini")) {
            intents.add(Intent.JADWAL_IBADAH_MINGGU_INI);
        } else if (containsAny(text, "jadwal ibadah", "ibadah", "kebaktian", "misa",
                "jadwal minggu", "ibadah minggu", "jadwal gereja",
                "jam ibadah", "kapan ibadah", "ibadah hari ini",
                "ibadah pagi", "ibadah malam", "kebaktian pemuda",
                "ibadah remaja", "ibadah anak", "ibadah umum",
                "kebaktian minggu", "liturgi")) {
            intents.add(Intent.JADWAL_IBADAH);
        }

        if (containsAny(text, "lokasi", "alamat", "di mana", "dimana",
                "tempat ibadah", "rumah ibadah", "gereja mana", "ada di",
                "gereja kita", "maps", "rute", "arah ke", "jalan ke",
                "google maps", "gps gereja")) {
            intents.add(Intent.LOKASI);
        }

        if (containsAny(text, "renungan", "devotion", "renungan harian",
                "bacaan", "kotbah", "khotbah", "firman hari ini",
                "bahan renungan", "renungan pagi", "renungan malam",
                "devotional", "pesan firman", "renungan minggu")) {
            intents.add(Intent.RENUNGAN);
        }

        if (containsAny(text, "pengumuman", "info gereja", "berita gereja",
                "pemberitahuan", "agenda", "acara gereja", "kegiatan gereja",
                "warta jemaat", "warta gereja")) {
            intents.add(Intent.PENGUMUMAN);
        }

        if (containsAny(text, "kontak", "hubungi", "telepon", "tlp",
                "nomor", "pengurus", "contact", "hp", "whatsapp", "wa",
                "gembala", "pendeta", "pastor", "nomor gereja")) {
            intents.add(Intent.KONTAK);
        }

        if (containsAny(text, "ayat", "alkitab", "kitab suci",
                "firman tuhan", "baca alkitab", "cari ayat")
                || extractVerseReference(text) != null) {
            intents.add(Intent.AYAT_ALKITAB);
        }

        if (containsAny(text, "tugas", "jadwal tugas", "pelayanan saya",
                "jadwal pelayanan", "tugas majelis", "jadwal saya", "piket")) {
            intents.add(Intent.JADWAL_TUGAS);
        }

        return intents;
    }


    /**
     * Gabungkan jawaban dari semua intent yang terdeteksi.
     * Setiap jawaban dipisah dengan garis pemisah.
     */
    private String prosesMultiIntent(List<Intent> intents, String input) {
        StringBuilder sb = new StringBuilder();
        String divider = "─".repeat(50);

        for (int i = 0; i < intents.size(); i++) {
            Intent intent = intents.get(i);

            sb.append(divider).append("\n");
            sb.append(getSectionTitle(intent)).append("\n");
            sb.append(divider).append("\n");

            String konten = getJawabanKonten(intent, input);
            sb.append(konten);

            if (i < intents.size() - 1) sb.append("\n\n");
        }

        return sb.toString().trim();
    }

    /** Judul section berdasarkan intent */
    private String getSectionTitle(Intent intent) {
        return switch (intent) {
            case JADWAL_IBADAH            -> "📅  JADWAL IBADAH";
            case JADWAL_IBADAH_MINGGU_INI -> "📅  JADWAL IBADAH MINGGU INI";
            case LOKASI        -> "📍  LOKASI GEREJA";
            case RENUNGAN      -> "🙏  RENUNGAN HARIAN";
            case PENGUMUMAN    -> "📢  PENGUMUMAN";
            case KONTAK        -> "📞  KONTAK PENGURUS";
            case JADWAL_TUGAS  -> "📋  JADWAL TUGAS";
            case AYAT_ALKITAB  -> "📖  AYAT ALKITAB";
            default            -> "ℹ️  INFORMASI";
        };
    }

    /** Isi konten saja, tanpa header. Jika kosong → "-" */
    private String getJawabanKonten(Intent intent, String input) {
        return switch (intent) {

            case JADWAL_IBADAH, JADWAL_IBADAH_MINGGU_INI -> {
                List<JadwalIbadah> list = (intent == Intent.JADWAL_IBADAH_MINGGU_INI)
                        ? jadwalIbadahDAO.getThisWeek()
                        : jadwalIbadahDAO.getUpcoming();
                if (list.isEmpty()) yield "-";
                StringBuilder sb = new StringBuilder();
                String lastDate = "";
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern(
                        "EEEE, dd MMMM yyyy", new Locale("id", "ID"));
                for (JadwalIbadah j : list) {
                    String dateKey = j.getTanggal() != null ? j.getTanggal().toString() : "";
                    if (!dateKey.equals(lastDate)) {
                        if (!lastDate.isEmpty()) sb.append("\n");
                        sb.append("📆 ")
                                .append(j.getTanggal() != null ? j.getTanggal().format(fmt) : "-")
                                .append("\n");
                        lastDate = dateKey;
                    }
                    sb.append(j.tampilkan()).append("\n");
                }
                yield sb.toString().trim();
            }

            case LOKASI -> {
                List<Lokasi> list = lokasiDAO.getAll();
                if (list.isEmpty()) yield "-";
                StringBuilder sb = new StringBuilder();
                for (Lokasi l : list) sb.append(l.tampilkan()).append("\n\n");
                yield sb.toString().trim();
            }

            case RENUNGAN -> {
                Renungan r = renunganDAO.getHariIni();
                if (r == null) yield "-";
                boolean isToday = r.getTanggal() != null
                        && r.getTanggal().equals(LocalDate.now());
                yield isToday
                        ? r.tampilkan()
                        : "⚠️ (Renungan terbaru)\n\n" + r.tampilkan();
            }

            case PENGUMUMAN -> {
                List<Pengumuman> list = pengumumanDAO.getLatest(5);
                if (list.isEmpty()) yield "-";
                StringBuilder sb = new StringBuilder();
                for (Pengumuman p : list) sb.append(p.tampilkan()).append("\n\n");
                yield sb.toString().trim();
            }

            case KONTAK -> {
                List<Lokasi> list = lokasiDAO.getAll();
                if (list.isEmpty()) yield "-";
                StringBuilder sb = new StringBuilder();
                for (Lokasi l : list) {
                    sb.append("🏛️ ").append(l.getNamaTempat()).append("\n");
                    sb.append("   Kontak : ").append(l.getKontak()).append("\n\n");
                }
                yield sb.toString().trim();
            }

            case JADWAL_TUGAS -> {
                if (loggedUser == null) yield "-";
                if ("majelis".equals(loggedUser.getRole())) {
                    List<JadwalTugas> list =
                            jadwalTugasDAO.getByMajelis(loggedUser.getIdUser());
                    if (list.isEmpty()) yield "-";
                    StringBuilder sb = new StringBuilder();
                    sb.append("Majelis: ").append(loggedUser.getNama()).append("\n\n");
                    for (JadwalTugas jt : list) sb.append(jt.tampilkan()).append("\n\n");
                    yield sb.toString().trim();
                } else if ("admin".equals(loggedUser.getRole())) {
                    List<JadwalTugas> list = jadwalTugasDAO.getAll();
                    if (list.isEmpty()) yield "-";
                    StringBuilder sb = new StringBuilder();
                    for (JadwalTugas jt : list) sb.append(jt.tampilkan()).append("\n\n");
                    yield sb.toString().trim();
                }
                yield "-";
            }

            case AYAT_ALKITAB -> {
                String ref = extractVerseReference(input);
                if (ref == null) yield "-";
                String result = queryAyat(ref);
                yield result
                        .replace("📖 AYAT ALKITAB\n" + "─".repeat(35) + "\n\n", "")
                        .trim();
            }

            default -> "-";
        };
    }

    // ────────────────────────────────────────────────────────────────────
    //  SINGLE INTENT DETECTION (fallback untuk prosesPertanyaan)
    // ────────────────────────────────────────────────────────────────────

    private Intent deteksiIntent(String text) {
        if (containsAny(text, "kamu itu apa", "siapa kamu", "siapa chataja",
                "apa itu chataja", "tentang chataja", "tentang bot"))
            return Intent.TENTANG_BOT;

        if (containsAny(text, "tuhan memberkati", "gbu", "god bless",
                "berkat tuhan", "kiranya tuhan", "shalom"))
            return Intent.UCAPAN_BERKAT;

        if (containsAny(text, "halo", "hai", "hi", "hello", "selamat pagi",
                "selamat siang", "selamat malam", "selamat sore", "hei"))
            return Intent.SAPAAN;

        if (containsAny(text, "bantuan", "help", "menu", "bisa apa",
                "apa saja", "fitur", "panduan", "cara pakai"))
            return Intent.BANTUAN;

        if (containsAny(text, "ayat", "alkitab", "kitab suci",
                "firman tuhan", "baca alkitab", "cari ayat")
                || extractVerseReference(text) != null)
            return Intent.AYAT_ALKITAB;

        if (containsAny(text, "doa", "berdoa", "doakan",
                "minta doa", "tolong doakan", "mohon doa"))
            return Intent.DOA;

        // Cek "minggu ini" lebih dulu (lebih spesifik)
        if (containsAny(text, "ibadah minggu ini", "jadwal ibadah minggu ini",
                "jadwal minggu ini", "kebaktian minggu ini"))
            return Intent.JADWAL_IBADAH_MINGGU_INI;

        if (containsAny(text, "jadwal ibadah", "ibadah", "kebaktian", "misa",
                "jadwal minggu", "ibadah minggu", "jadwal gereja",
                "jam ibadah", "kapan ibadah", "ibadah hari ini",
                "ibadah pagi", "ibadah malam", "kebaktian pemuda",
                "ibadah remaja", "ibadah anak", "ibadah umum",
                "kebaktian minggu", "liturgi"))
            return Intent.JADWAL_IBADAH;

        if (containsAny(text, "lokasi", "alamat", "di mana", "dimana",
                "tempat ibadah", "rumah ibadah", "gereja mana", "ada di",
                "gereja kita", "maps", "rute", "arah ke", "jalan ke",
                "google maps", "gps gereja"))
            return Intent.LOKASI;

        if (containsAny(text, "renungan", "devotion", "renungan harian",
                "bacaan", "kotbah", "khotbah", "firman hari ini",
                "bahan renungan", "renungan pagi", "renungan malam",
                "devotional", "pesan firman", "renungan minggu"))
            return Intent.RENUNGAN;

        if (containsAny(text, "pengumuman", "info", "berita", "kabar",
                "pemberitahuan", "agenda", "acara gereja", "kegiatan gereja",
                "event", "ada acara apa", "program gereja",
                "warta jemaat", "warta gereja"))
            return Intent.PENGUMUMAN;

        if (containsAny(text, "kontak", "hubungi", "telepon", "tlp",
                "nomor", "pengurus", "contact", "hp", "whatsapp", "wa",
                "gembala", "pendeta", "pastor", "nomor gereja"))
            return Intent.KONTAK;

        if (containsAny(text, "tugas", "jadwal tugas", "pelayanan saya",
                "jadwal pelayanan", "tugas majelis", "jadwal saya", "piket"))
            return Intent.JADWAL_TUGAS;

        return Intent.TIDAK_DIKENALI;
    }

    private boolean containsAny(String text, String... keywords) {
        for (String kw : keywords) {
            if (text.contains(kw)) return true;
        }
        return false;
    }

    // ────────────────────────────────────────────────────────────────────
    //  BIBLE — query dari holybible.db (offline, no API)
    // ────────────────────────────────────────────────────────────────────

    private String extractVerseReference(String text) {
        Pattern p = Pattern.compile(
            "(?i)(\\d?\\s*[a-z]+(?:\\s+[a-z]+)?)\\s+(\\d+):(\\d+)(?:-(\\d+))?");
        Matcher m = p.matcher(text);
        if (m.find()) {
            String book     = m.group(1).trim();
            String chapter  = m.group(2);
            String verse    = m.group(3);
            String endVerse = m.group(4);
            return endVerse != null
                    ? book + " " + chapter + ":" + verse + "-" + endVerse
                    : book + " " + chapter + ":" + verse;
        }
        return null;
    }

    private String queryAyat(String reference) {
        if (!ayatDAO.isAvailable()) {
            return "❌ File holybible.db tidak ditemukan.\n"
                 + "Pastikan file holybible.db ada di direktori yang sama dengan chataja.db.";
        }

        String[] parts = parseReference(reference);
        if (parts == null) {
            return "⚠️ Format referensi tidak dikenali: \"" + reference + "\"\n"
                 + "Contoh: \"Yohanes 3:16\" atau \"Mazmur 23:1-6\"";
        }

        String namaKitab  = parts[0];
        int    chapter    = Integer.parseInt(parts[1]);
        int    verseStart = Integer.parseInt(parts[2]);
        int    verseEnd   = parts[3] != null ? Integer.parseInt(parts[3]) : -1;

        Integer bookNum = ayatDAO.getBookNumber(namaKitab);
        if (bookNum == null) {
            return "❌ Nama kitab tidak dikenali: \"" + namaKitab + "\"\n"
                 + "Coba gunakan nama lengkap, misal: \"Yohanes\", \"Mazmur\", \"1 Korintus\"";
        }

        if (verseEnd == -1) {
            AyatAlkitab ayat = ayatDAO.getAyat(bookNum, chapter, verseStart);
            if (ayat == null) {
                return "❌ Ayat tidak ditemukan: " + reference + "\n"
                     + "Periksa nama kitab, pasal, dan nomor ayat.";
            }
            return ayat.tampilkan();
        }

        List<AyatAlkitab> list = ayatDAO.getRangeAyat(bookNum, chapter, verseStart, verseEnd);
        if (list.isEmpty()) {
            return "❌ Ayat tidak ditemukan: " + reference + "\n"
                 + "Periksa nama kitab, pasal, dan nomor ayat.";
        }
        return formatRangeAyat(list, reference);
    }

    private String[] parseReference(String reference) {
        Pattern p = Pattern.compile(
            "(?i)(\\d?\\s*[a-z]+(?:[\\s-][a-z]+)*)\\s+(\\d+):(\\d+)(?:-(\\d+))?");
        Matcher m = p.matcher(reference.trim());
        if (!m.find()) return null;

        String namaKitab  = m.group(1).trim().toLowerCase().replaceAll("\\s+", "-");
        String chapter    = m.group(2);
        String verseStart = m.group(3);
        String verseEnd   = m.group(4);

        return new String[]{namaKitab, chapter, verseStart, verseEnd};
    }

    private String formatRangeAyat(List<AyatAlkitab> list, String reference) {
        StringBuilder sb = new StringBuilder();
        sb.append("📖 AYAT ALKITAB\n");
        sb.append("─".repeat(35)).append("\n\n");

        AyatAlkitab first = list.get(0);
        AyatAlkitab last  = list.get(list.size() - 1);
        String ref = first.getNamaKitab() + " "
                   + first.getChapter() + ":" + first.getVersecount()
                   + "-" + last.getVersecount();

        sb.append("🔖 ").append(ref).append("\n\n");
        for (AyatAlkitab a : list) {
            sb.append(a.getVersecount()).append(". ").append(a.getVerse().trim()).append("\n");
        }
        sb.append("\n— Alkitab Terjemahan Baru (TB)");
        return sb.toString();
    }

    // ────────────────────────────────────────────────────────────────────
    //  RESPONSE BUILDERS — UC-1 s.d. UC-8
    // ────────────────────────────────────────────────────────────────────

    private String responJadwalIbadah() {
        List<JadwalIbadah> list = jadwalIbadahDAO.getUpcoming();
        if (list.isEmpty()) {
            return "📅 Jadwal ibadah belum tersedia saat ini.\n"
                 + "Silakan hubungi pengurus gereja untuk informasi lebih lanjut.";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("📅 JADWAL IBADAH GEREJA\n");
        sb.append("─".repeat(35)).append("\n\n");
        String lastDate = "";
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy", new Locale("id", "ID"));
        for (JadwalIbadah j : list) {
            String dateKey = j.getTanggal() != null ? j.getTanggal().toString() : "";
            if (!dateKey.equals(lastDate)) {
                if (!lastDate.isEmpty()) sb.append("\n");
                sb.append("📆 ")
                  .append(j.getTanggal() != null ? j.getTanggal().format(fmt) : "-")
                  .append("\n");
                lastDate = dateKey;
            }
            sb.append(j.tampilkan()).append("\n");
        }
        return sb.toString().trim();
    }

    private String responJadwalIbadahMingguIni() {
        List<JadwalIbadah> list = jadwalIbadahDAO.getThisWeek();
        if (list.isEmpty()) {
            return "📅 Tidak ada jadwal ibadah untuk minggu ini.\n"
                 + "Silakan tanyakan \"jadwal ibadah\" untuk melihat jadwal mendatang.";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("📅 JADWAL IBADAH MINGGU INI\n");
        sb.append("─".repeat(35)).append("\n\n");
        String lastDate = "";
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy", new Locale("id", "ID"));
        for (JadwalIbadah j : list) {
            String dateKey = j.getTanggal() != null ? j.getTanggal().toString() : "";
            if (!dateKey.equals(lastDate)) {
                if (!lastDate.isEmpty()) sb.append("\n");
                sb.append("📆 ")
                  .append(j.getTanggal() != null ? j.getTanggal().format(fmt) : "-")
                  .append("\n");
                lastDate = dateKey;
            }
            sb.append(j.tampilkan()).append("\n");
        }
        return sb.toString().trim();
    }

    private String responLokasi() {
        List<Lokasi> list = lokasiDAO.getAll();
        if (list.isEmpty()) {
            return "📍 Informasi lokasi gereja belum tersedia.\n"
                 + "Silakan hubungi pengurus untuk informasi lebih lanjut.";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("📍 LOKASI RUMAH IBADAH\n");
        sb.append("─".repeat(35)).append("\n\n");
        for (Lokasi l : list) sb.append(l.tampilkan()).append("\n\n");
        return sb.toString().trim();
    }

    private String responRenungan() {
        Renungan r = renunganDAO.getHariIni();
        if (r == null)
            return "📖 Renungan harian sedang disiapkan.\nSilakan cek kembali nanti.";
        boolean isToday = r.getTanggal() != null && r.getTanggal().equals(LocalDate.now());
        String prefix = isToday ? "" : "\n⚠️ (Renungan terbaru yang tersedia)\n\n";
        return prefix + r.tampilkan();
    }

    private String responPengumuman() {
        List<Pengumuman> list = pengumumanDAO.getLatest(5);
        if (list.isEmpty()) return "📢 Belum ada pengumuman saat ini.";
        StringBuilder sb = new StringBuilder();
        sb.append("📢 PENGUMUMAN GEREJA\n");
        sb.append("─".repeat(35)).append("\n\n");
        for (Pengumuman p : list) sb.append(p.tampilkan()).append("\n\n");
        return sb.toString().trim();
    }

    private String responKontak() {
        List<Lokasi> list = lokasiDAO.getAll();
        if (list.isEmpty()) {
            return "📞 Informasi kontak pengurus belum tersedia.\n"
                 + "Silakan kunjungi gereja secara langsung.";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("📞 KONTAK PENGURUS GEREJA\n");
        sb.append("─".repeat(35)).append("\n\n");
        for (Lokasi l : list) {
            sb.append("🏛️ ").append(l.getNamaTempat()).append("\n");
            sb.append("   Kontak : ").append(l.getKontak()).append("\n\n");
        }
        return sb.toString().trim();
    }

    private String responJadwalTugas() {
        if (loggedUser == null || !"majelis".equals(loggedUser.getRole())) {
            return "🔒 Fitur ini hanya tersedia untuk Majelis yang telah login.\n"
                 + "Silakan login terlebih dahulu melalui tombol Login di bawah.";
        }
        List<JadwalTugas> list = jadwalTugasDAO.getByMajelis(loggedUser.getIdUser());
        if (list.isEmpty())
            return "📋 Anda belum memiliki jadwal tugas pelayanan untuk saat ini.";
        StringBuilder sb = new StringBuilder();
        sb.append("📋 JADWAL TUGAS PELAYANAN\n");
        sb.append("Majelis: ").append(loggedUser.getNama()).append("\n");
        sb.append("─".repeat(35)).append("\n\n");
        for (JadwalTugas jt : list) sb.append(jt.tampilkan()).append("\n\n");
        return sb.toString().trim();
    }

    private String responSapaan() {
        String nama = (loggedUser != null) ? ", " + loggedUser.getNama() : "";
        return "👋 Halo" + nama + "! Saya " + namaBot + ", asisten informasi gereja Anda.\n\n"
             + "Saya siap membantu Anda dengan:\n"
             + "• 📅 Jadwal ibadah\n"
             + "• 📍 Lokasi & alamat gereja\n"
             + "• 📖 Ayat Alkitab (cth: \"Yohanes 3:16\")\n"
             + "• 🙏 Renungan harian\n"
             + "• 📢 Pengumuman gereja\n"
             + "• 📞 Kontak pengurus\n\n"
             + "💡 Tip: Anda bisa tanya beberapa hal sekaligus!\n"
             + "   Cth: \"Berikan renungan hari ini dan jadwal ibadah minggu ini\"\n\n"
             + "Ketik \"bantuan\" untuk panduan lengkap. 😊";
    }

    private String responBantuan() {
        return "ℹ️ PANDUAN PENGGUNAAN ChatAja\n"
             + "─".repeat(35) + "\n\n"
             + "📅 Jadwal Ibadah\n"
             + "   → \"Jadwal ibadah minggu ini\"\n"
             + "   → \"Jam berapa kebaktian pagi?\"\n\n"
             + "📍 Lokasi Gereja\n"
             + "   → \"Di mana alamat gereja?\"\n\n"
             + "📖 Ayat Alkitab (offline, 31.104 ayat TB)\n"
             + "   → \"Yohanes 3:16\"\n"
             + "   → \"Mazmur 23:1-6\"\n"
             + "   → \"Ayat tentang kasih\"\n"
             + "   → \"Ayat alkitab acak\"\n\n"
             + "🙏 Renungan Harian\n"
             + "   → \"Berikan renungan hari ini\"\n\n"
             + "📢 Pengumuman\n"
             + "   → \"Pengumuman gereja terbaru\"\n\n"
             + "📞 Kontak Pengurus\n"
             + "   → \"Nomor telepon pendeta\"\n\n"
             + "📋 Jadwal Tugas (khusus Majelis login)\n"
             + "   → \"Jadwal pelayanan saya\"\n\n"
             + "💡 MULTI-PERTANYAAN (fitur baru!)\n"
             + "   → \"Renungan hari ini dan jadwal ibadah\"\n"
             + "   → \"Lokasi gereja beserta kontaknya\"\n"
             + "   → \"Pengumuman, renungan, dan jadwal ibadah\"";
    }

    // ────────────────────────────────────────────────────────────────────
    //  RESPONSE BUILDERS — fitur baru
    // ────────────────────────────────────────────────────────────────────

    private String responAyatAlkitab(String input) {
        String lower = input.toLowerCase();

        String ref = extractVerseReference(input);
        if (ref != null) return queryAyat(ref);

        if (containsAny(lower, "tentang kasih", "soal kasih"))
            return queryAyat("Yohanes 3:16");
        if (containsAny(lower, "tentang iman", "soal iman"))
            return queryAyat("Ibrani 11:1");
        if (containsAny(lower, "tentang kekuatan", "soal kekuatan", "tetap kuat"))
            return queryAyat("Filipi 4:13");
        if (containsAny(lower, "tentang damai", "ketenangan", "tidak takut"))
            return queryAyat("Yohanes 14:27");
        if (containsAny(lower, "tentang harapan", "tentang pengharapan"))
            return queryAyat("Yeremia 29:11");
        if (containsAny(lower, "tentang sukacita", "sukacita", "bersukacita"))
            return queryAyat("Filipi 4:4");
        if (containsAny(lower, "tentang keselamatan", "diselamatkan"))
            return queryAyat("Yohanes 3:17");
        if (containsAny(lower, "tentang hikmat", "hikmat", "bijaksana"))
            return queryAyat("Amsal 3:5");
        if (containsAny(lower, "tentang perlindungan", "dilindungi"))
            return queryAyat("Mazmur 23:1");
        if (containsAny(lower, "tentang pengampunan", "diampuni", "tobat"))
            return queryAyat("1 Yohanes 1:9");
        if (containsAny(lower, "tentang kesabaran", "sabar", "tabah"))
            return queryAyat("Roma 5:3");
        if (containsAny(lower, "tentang kasih karunia", "anugerah", "rahmat"))
            return queryAyat("Efesus 2:8");

        if (containsAny(lower, "acak", "random", "sembarang", "ayat apa saja")) {
            AyatAlkitab ayat = ayatDAO.getAyatAcak();
            return ayat != null ? ayat.tampilkan()
                    : "❌ Gagal mengambil ayat. Pastikan holybible.db tersedia.";
        }

        if (containsAny(lower, "cari ayat", "cari kata")) {
            String keyword = lower.replace("cari ayat", "").replace("cari kata", "").trim();
            if (!keyword.isEmpty()) return cariAyat(keyword);
        }

        return "📖 AYAT ALKITAB\n"
             + "─".repeat(35) + "\n\n"
             + "Saya bisa mencari ayat dari 31.104 ayat Alkitab TB!\n\n"
             + "Cara bertanya:\n"
             + "• Referensi spesifik : \"Yohanes 3:16\"\n"
             + "• Rentang ayat       : \"Mazmur 23:1-6\"\n"
             + "• Kitab bernomor     : \"1 Korintus 13:4\"\n"
             + "• Cari kata          : \"Cari ayat kasih\"\n\n"
             + "Atau berdasarkan topik:\n"
             + "• \"Ayat tentang kasih\"\n"
             + "• \"Ayat tentang iman\"\n"
             + "• \"Ayat tentang kekuatan\"\n"
             + "• \"Ayat tentang damai\"\n"
             + "• \"Ayat tentang hikmat\"\n"
             + "• \"Ayat tentang pengampunan\"\n"
             + "• \"Ayat alkitab acak\"\n";
    }

    private String cariAyat(String keyword) {
        List<AyatAlkitab> list = ayatDAO.searchAyat(keyword, 5);
        if (list.isEmpty()) {
            return "🔍 Tidak ditemukan ayat yang mengandung kata \"" + keyword + "\".";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("🔍 HASIL PENCARIAN: \"").append(keyword).append("\"\n");
        sb.append("─".repeat(35)).append("\n\n");
        for (AyatAlkitab a : list) {
            sb.append("🔖 ").append(a.getNamaKitab())
              .append(" ").append(a.getChapter())
              .append(":").append(a.getVersecount()).append("\n");
            sb.append(a.getVerse().trim()).append("\n\n");
        }
        sb.append("— Alkitab Terjemahan Baru (TB)");
        return sb.toString();
    }

    private String responDoa() {
        return "🙏 DOA & PERSEKUTUAN\n"
             + "─".repeat(35) + "\n\n"
             + "\"Janganlah hendaknya kamu kuatir tentang apapun juga, "
             + "tetapi nyatakanlah dalam segala hal keinginanmu kepada Allah "
             + "dalam doa dan permohonan dengan ucapan syukur.\"\n\n"
             + "— Filipi 4:6\n\n"
             + "Untuk permohonan doa pribadi:\n"
             + "📞 Hubungi pengurus gereja (ketik \"kontak\")\n"
             + "🕐 Hadir dalam ibadah doa gereja\n"
             + "📋 Jadwal ibadah doa → ketik \"jadwal ibadah\"";
    }

    private String responTentangBot() {
        return "ℹ️ TENTANG CHATAJA\n"
             + "─".repeat(35) + "\n\n"
             + "Saya adalah " + namaBot + "\n"
             + "Asisten informasi resmi gereja ini.\n\n"
             + "Yang bisa saya bantu:\n"
             + "📅 Jadwal ibadah mingguan\n"
             + "📍 Lokasi & kontak gereja\n"
             + "📖 Ayat Alkitab TB (31.104 ayat, offline)\n"
             + "📢 Pengumuman & warta jemaat\n"
             + "🙏 Renungan harian\n"
             + "📋 Jadwal tugas majelis\n"
             + "💡 Multi-pertanyaan dalam satu kalimat\n\n"
             + "Ketik \"bantuan\" untuk panduan lengkap.";
    }

    private String responUcapanBerkat() {
        AyatAlkitab ayat = ayatDAO.getAyat(44, 1, 7); // Roma 1:7
        String teks = (ayat != null) ? ayat.getVerse()
                : "Kasih karunia dan damai sejahtera dari Allah, Bapa kita, "
                + "dan dari Tuhan Yesus Kristus menyertai kamu.";
        return "🙏 Tuhan Yesus memberkati Anda juga!\n\n"
             + "\"" + teks + "\"\n\n"
             + "— Roma 1:7\n\n"
             + "Semoga hari Anda dipenuhi berkat Tuhan. 😊";
    }

    private String responTidakDikenali(String input) {
        return "🤔 Maaf, saya tidak memahami: \"" + input + "\"\n\n"
             + "Coba tanyakan:\n"
             + "• \"Jadwal ibadah\"\n"
             + "• \"Yohanes 3:16\" (ayat Alkitab)\n"
             + "• \"Renungan hari ini\"\n\n"
             + "Atau ketik \"bantuan\" untuk daftar lengkap. 😊";
    }
}