package com.chataja.chatbot;

import com.chataja.dao.*;
import com.chataja.model.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatBot {

    private final String namaBot = "ChatAja";
    private User loggedUser;

    // ── DAO ──────────────────────────────────────────────────────────────
    private final JadwalIbadahDAO  jadwalIbadahDAO  = new JadwalIbadahDAO();
    private final LokasiDAO        lokasiDAO        = new LokasiDAO();
    private final RenunganDAO      renunganDAO      = new RenunganDAO();
    private final PengumumanDAO    pengumumanDAO    = new PengumumanDAO();
    private final JadwalTugasDAO   jadwalTugasDAO   = new JadwalTugasDAO();
    private final AyatAlkitabDAO   ayatDAO          = new AyatAlkitabDAO();

    // ── Enum Intent ───────────────────────────────────────────────────────
    private enum Intent {
        JADWAL_IBADAH, LOKASI, RENUNGAN, PENGUMUMAN, KONTAK,
        JADWAL_TUGAS, SAPAAN, BANTUAN, TIDAK_DIKENALI,
        AYAT_ALKITAB, DOA, TENTANG_BOT, UCAPAN_BERKAT
    }

    public ChatBot() {}
    public ChatBot(User loggedUser) { this.loggedUser = loggedUser; }

    public void setLoggedUser(User user) { this.loggedUser = user; }
    public User getLoggedUser()          { return loggedUser; }
    public String getNamaBot()           { return namaBot; }

    // ────────────────────────────────────────────────────────────────────
    //  PUBLIC INTERFACE
    // ────────────────────────────────────────────────────────────────────

    public String prosesPertanyaan(String input) {
        if (!validasiInput(input)) {
            return "⚠️ Mohon masukkan pertanyaan yang valid (tidak boleh kosong).";
        }
        Intent intent = deteksiIntent(normalizeText(input));
        return displayJawaban(intent, input);
    }

    public boolean validasiInput(String input) {
        return input != null && !input.trim().isEmpty();
    }

    public String displayJawaban(Intent intent, String input) {
        return switch (intent) {
            case JADWAL_IBADAH -> responJadwalIbadah();
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

    /**
     * Dipakai UI agar deteksi prompt pengumuman konsisten dengan engine intent (termasuk typo/spasi).
     */
    public boolean isPengumumanQuery(String input) {
        if (!validasiInput(input)) return false;
        String text = normalizeText(input);
        return containsAny(text, "pengumuman", "info", "berita", "kabar",
                "pemberitahuan", "agenda", "acara gereja", "kegiatan gereja",
                "event", "ada acara apa", "program gereja",
                "warta jemaat", "warta gereja", "info gereja", "berita gereja");
    }

    /**
     * Dipakai UI agar deteksi prompt renungan konsisten dengan engine intent (termasuk typo/spasi).
     */
    public boolean isRenunganQuery(String input) {
        if (!validasiInput(input)) return false;
        String text = normalizeText(input);
        return containsAny(text, "renungan", "devotion", "renungan harian",
                "bacaan", "kotbah", "khotbah", "firman hari ini",
                "bahan renungan", "renungan pagi", "renungan malam",
                "devotional", "pesan firman", "renungan minggu");
    }

    // ────────────────────────────────────────────────────────────────────
    //  INTENT DETECTION
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
        String normalizedText = normalizeText(text);
        for (String kw : keywords) {
            if (isKeywordMatch(normalizedText, normalizeText(kw))) return true;
        }
        return false;
    }

    private String normalizeText(String text) {
        if (text == null) return "";
        return text.toLowerCase()
                .replaceAll("[^\\p{L}\\p{N}\\s:.-]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String removeSpaces(String text) {
        return text.replace(" ", "");
    }

    private boolean isKeywordMatch(String text, String keyword) {
        if (text.isEmpty() || keyword.isEmpty()) return false;
        if (text.contains(keyword)) return true;

        String compactText = removeSpaces(text);
        String compactKeyword = removeSpaces(keyword);
        if (compactText.contains(compactKeyword)) return true;

        if (fuzzyContainsByWindow(text, keyword)) return true;
        return levenshteinDistance(compactText, compactKeyword) <= typoThreshold(compactKeyword.length());
    }

    private boolean fuzzyContainsByWindow(String text, String keyword) {
        String[] textWords = text.split(" ");
        String[] keywordWords = keyword.split(" ");
        if (textWords.length == 0 || keywordWords.length == 0) return false;

        int window = keywordWords.length;
        int threshold = typoThreshold(keyword.length());

        if (window == 1) {
            String target = keywordWords[0];
            for (String word : textWords) {
                if (levenshteinDistance(word, target) <= typoThreshold(target.length())) {
                    return true;
                }
            }
            return false;
        }

        for (int i = 0; i <= textWords.length - window; i++) {
            String candidate = String.join(" ", java.util.Arrays.copyOfRange(textWords, i, i + window));
            if (levenshteinDistance(candidate, keyword) <= threshold) {
                return true;
            }
        }
        return false;
    }

    private int typoThreshold(int len) {
        if (len <= 4) return 1;
        if (len <= 10) return 2;
        return 3;
    }

    private int levenshteinDistance(String a, String b) {
        if (a.equals(b)) return 0;
        int n = a.length();
        int m = b.length();
        if (n == 0) return m;
        if (m == 0) return n;

        int[] prev = new int[m + 1];
        int[] curr = new int[m + 1];
        for (int j = 0; j <= m; j++) prev[j] = j;

        for (int i = 1; i <= n; i++) {
            curr[0] = i;
            char ca = a.charAt(i - 1);
            for (int j = 1; j <= m; j++) {
                int cost = (ca == b.charAt(j - 1)) ? 0 : 1;
                curr[j] = Math.min(
                        Math.min(curr[j - 1] + 1, prev[j] + 1),
                        prev[j - 1] + cost
                );
            }
            int[] tmp = prev;
            prev = curr;
            curr = tmp;
        }
        return prev[m];
    }

    // ────────────────────────────────────────────────────────────────────
    //  BIBLE — query dari holybible.db (offline, no API)
    // ────────────────────────────────────────────────────────────────────

    /**
     * Ekstrak referensi ayat dari teks pengguna.
     * Contoh: "Yohanes 3:16", "Mazmur 23:1-6", "1 Korintus 13:4"
     */
    private String extractVerseReference(String text) {
        String normalized = normalizeText(text);
        Pattern p = Pattern.compile(
                "(?i)(\\d?\\s*[a-z]+(?:\\s+[a-z]+)?)\\s+(\\d+):(\\d+)(?:-(\\d+))?");
        Matcher m = p.matcher(normalized);
        if (m.find()) {
            String book     = m.group(1).trim();
            String chapter  = m.group(2);
            String verse    = m.group(3);
            String endVerse = m.group(4);
            return endVerse != null
                    ? book + " " + chapter + ":" + verse + "-" + endVerse
                    : book + " " + chapter + ":" + verse;
        }

        Pattern compact = Pattern.compile(
                "(?i)(\\d?[a-z]+(?:[a-z-]+)?)(\\d+):(\\d+)(?:-(\\d+))?");
        Matcher mc = compact.matcher(removeSpaces(normalized));
        if (mc.find()) {
            String book = mc.group(1).trim();
            String chapter = mc.group(2);
            String verse = mc.group(3);
            String endVerse = mc.group(4);
            return endVerse != null
                    ? book + " " + chapter + ":" + verse + "-" + endVerse
                    : book + " " + chapter + ":" + verse;
        }
        return null;
    }

    /**
     * Query ayat dari holybible.db berdasarkan referensi teks.
     * Menggantikan callBibleApi() — tidak butuh internet.
     *
     * @param reference contoh: "Yohanes 3:16" atau "Mazmur 23:1-6"
     */
    private String queryAyat(String reference) {
        // Cek database tersedia
        if (!ayatDAO.isAvailable()) {
            return "❌ File holybible.db tidak ditemukan.\n"
                    + "Pastikan file holybible.db ada di direktori yang sama dengan chataja.db.";
        }

        // Parse referensi
        String[] parts = parseReference(reference);
        if (parts == null) {
            return "⚠️ Format referensi tidak dikenali: \"" + reference + "\"\n"
                    + "Contoh: \"Yohanes 3:16\" atau \"Mazmur 23:1-6\"";
        }

        String namaKitab  = parts[0]; // "yohanes"
        int    chapter    = Integer.parseInt(parts[1]); // 3
        int    verseStart = Integer.parseInt(parts[2]); // 16
        int    verseEnd   = parts[3] != null ? Integer.parseInt(parts[3]) : -1; // -1 jika single

        // Lookup nomor kitab
        Integer bookNum = ayatDAO.getBookNumber(namaKitab);
        if (bookNum == null) {
            return "❌ Nama kitab tidak dikenali: \"" + namaKitab + "\"\n"
                    + "Coba gunakan nama lengkap, misal: \"Yohanes\", \"Mazmur\", \"1 Korintus\"";
        }

        // Single ayat
        if (verseEnd == -1) {
            AyatAlkitab ayat = ayatDAO.getAyat(bookNum, chapter, verseStart);
            if (ayat == null) {
                return "❌ Ayat tidak ditemukan: " + reference + "\n"
                        + "Periksa nama kitab, pasal, dan nomor ayat.";
            }
            return ayat.tampilkan();
        }

        // Range ayat
        List<AyatAlkitab> list = ayatDAO.getRangeAyat(bookNum, chapter, verseStart, verseEnd);
        if (list.isEmpty()) {
            return "❌ Ayat tidak ditemukan: " + reference + "\n"
                    + "Periksa nama kitab, pasal, dan nomor ayat.";
        }
        return formatRangeAyat(list, reference);
    }

    /**
     * Parse referensi teks menjadi [namaKitab, chapter, verseStart, verseEnd].
     * "Yohanes 3:16"   → ["yohanes", "3", "16", null]
     * "Mazmur 23:1-6"  → ["mazmur", "23", "1", "6"]
     * "1 Korintus 13:4" → ["1-korintus", "13", "4", null]
     */
    private String[] parseReference(String reference) {
        Pattern p = Pattern.compile(
                "(?i)(\\d?\\s*[a-z]+(?:[\\s-][a-z]+)*)\\s+(\\d+):(\\d+)(?:-(\\d+))?");
        Matcher m = p.matcher(reference.trim());
        if (!m.find()) return null;

        String namaKitab  = m.group(1).trim().toLowerCase().replaceAll("\\s+", "-");
        String chapter    = m.group(2);
        String verseStart = m.group(3);
        String verseEnd   = m.group(4); // null jika single ayat

        return new String[]{namaKitab, chapter, verseStart, verseEnd};
    }

    /** Format tampilan range ayat */
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

    // ── Public accessor methods untuk ChatView ──────────────────────────

    /**
     * Digunakan oleh ChatView untuk menampilkan renungan di sidebar/panel.
     */
    public Renungan getRenunganHariIni() {
        return renunganDAO.getHariIni();
    }

    /**
     * Digunakan oleh ChatView untuk menampilkan pengumuman terbaru.
     */
    public List<Pengumuman> getLatestPengumuman(int limit) {
        return pengumumanDAO.getLatest(limit);
    }

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
                + "Ketik \"bantuan\" untuk panduan lengkap. 😊";
    }

    private String responBantuan() {
        return "ℹ️ PANDUAN PENGGUNAAN ChatAja\n"
                + "─".repeat(35) + "\n\n"
                + "📅 Jadwal Ibadah\n"
                + "   → \"Jadwal ibadah minggu ini\"\n"
                + "   → \"Jam berapa kebaktian pagi?\"\n\n"
                + "📍 Lokasi Gereja\n"
                + "   → \"Di mana alamat gereja?\"\n"
                + "   → \"Rute ke gereja\"\n\n"
                + "📖 Ayat Alkitab (offline, 31.104 ayat TB)\n"
                + "   → \"Yohanes 3:16\"\n"
                + "   → \"Mazmur 23:1-6\" (range ayat)\n"
                + "   → \"Ayat tentang kasih\"\n"
                + "   → \"Ayat tentang kekuatan\"\n"
                + "   → \"Cari ayat kasih\" (pencarian kata)\n"
                + "   → \"Ayat alkitab acak\"\n\n"
                + "🙏 Renungan Harian\n"
                + "   → \"Berikan renungan hari ini\"\n\n"
                + "📢 Pengumuman\n"
                + "   → \"Pengumuman gereja terbaru\"\n\n"
                + "📞 Kontak Pengurus\n"
                + "   → \"Nomor telepon pendeta\"\n\n"
                + "📋 Jadwal Tugas (khusus Majelis login)\n"
                + "   → \"Jadwal pelayanan saya\"";
    }

    // ────────────────────────────────────────────────────────────────────
    //  RESPONSE BUILDERS — fitur baru
    // ────────────────────────────────────────────────────────────────────

    private String responAyatAlkitab(String input) {
        String lower = input.toLowerCase();

        // 1. Cari referensi spesifik (Yohanes 3:16, Mazmur 23:1-6, dst)
        String ref = extractVerseReference(input);
        if (ref != null) return queryAyat(ref);

        // 2. Cocokkan topik → referensi populer
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

        // 3. Ayat acak
        if (containsAny(lower, "acak", "random", "sembarang", "ayat apa saja")) {
            AyatAlkitab ayat = ayatDAO.getAyatAcak();
            return ayat != null ? ayat.tampilkan()
                    : "❌ Gagal mengambil ayat. Pastikan holybible.db tersedia.";
        }

        // 4. Pencarian kata kunci — "cari ayat kasih", "ayat tentang xxx"
        if (containsAny(lower, "cari ayat", "cari kata")) {
            String keyword = lower.replace("cari ayat", "").replace("cari kata", "").trim();
            if (!keyword.isEmpty()) return cariAyat(keyword);
        }

        // 5. Panduan
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

    /** Cari ayat berdasarkan kata kunci dalam isi ayat */
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
                + "📋 Jadwal tugas majelis\n\n"
                + "Ketik \"bantuan\" untuk panduan lengkap.";
    }

    private String responUcapanBerkat() {
        // Ambil langsung dari DB
        AyatAlkitab ayat = ayatDAO.getAyat(44, 1, 7); // Roma 1:7
        String teks = (ayat != null) ? ayat.getVerse() :
                "Kasih karunia dan damai sejahtera dari Allah, Bapa kita, " +
                        "dan dari Tuhan Yesus Kristus menyertai kamu.";
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
