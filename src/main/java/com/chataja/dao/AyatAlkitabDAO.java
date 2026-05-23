package com.chataja.dao;

import com.chataja.model.AyatAlkitab;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class AyatAlkitabDAO {

    private static final String DB_URL = "jdbc:sqlite:holybible.db";

    /** Koneksi ke holybible.db */
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    // ── MAPPING: nama kitab Indonesia → nomor Book (0-65) ────────────────

    /**
     * Mapping nama kitab Indonesia ke nomor Book di holybible.db.
     * Urutan standar Alkitab (0-indexed).
     */
    public Integer getBookNumber(String namaKitab) {
        if (namaKitab == null) return null;
        return switch (namaKitab.toLowerCase().replaceAll("\\s+", "-").trim()) {
            // ── Perjanjian Lama ──────────────────────────────────────────
            case "kej", "kejadian"          -> 0;
            case "kel", "keluaran"          -> 1;
            case "ima", "imamat"            -> 2;
            case "bil", "bilangan"          -> 3;
            case "ul",  "ulangan"           -> 4;
            case "yos", "yosua"             -> 5;
            case "hak", "hakim-hakim"       -> 6;
            case "rut"                      -> 7;
            case "1-sam", "1-samuel"        -> 8;
            case "2-sam", "2-samuel"        -> 9;
            case "1-raj", "1-raja-raja"     -> 10;
            case "2-raj", "2-raja-raja"     -> 11;
            case "1-taw", "1-tawarikh"      -> 12;
            case "2-taw", "2-tawarikh"      -> 13;
            case "ezr",  "ezra"             -> 14;
            case "neh",  "nehemia"          -> 15;
            case "est",  "ester"            -> 16;
            case "ayb",  "ayub"             -> 17;
            case "mzm",  "mazmur", "maz"    -> 18;
            case "ams",  "amsal"            -> 19;
            case "pkh",  "pengkhotbah"      -> 20;
            case "kid",  "kidung-agung"     -> 21;
            case "yes",  "yesaya"           -> 22;
            case "yer",  "yeremia"          -> 23;
            case "rat",  "ratapan"          -> 24;
            case "yeh",  "yehezkiel"        -> 25;
            case "dan",  "daniel"           -> 26;
            case "hos",  "hosea"            -> 27;
            case "yl",   "yoel"             -> 28;
            case "am",   "amos"             -> 29;
            case "ob",   "obaja"            -> 30;
            case "yun",  "yunus"            -> 31;
            case "mi",   "mikha"            -> 32;
            case "nah",  "nahum"            -> 33;
            case "hab",  "habakuk"          -> 34;
            case "zef",  "zefanya"          -> 35;
            case "hag",  "hagai"            -> 36;
            case "zak",  "zakharia"         -> 37;
            case "mal",  "maleakhi"         -> 38;
            // ── Perjanjian Baru ──────────────────────────────────────────
            case "mat",  "matius"           -> 39;
            case "mrk",  "markus"           -> 40;
            case "luk",  "lukas"            -> 41;
            case "yoh",  "yohanes"          -> 42;
            case "kis",  "kisah-para-rasul" -> 43;
            case "rm",   "roma"             -> 44;
            case "1-kor", "1-korintus"      -> 45;
            case "2-kor", "2-korintus"      -> 46;
            case "gal",  "galatia"          -> 47;
            case "ef",   "efesus"           -> 48;
            case "flp",  "filipi"           -> 49;
            case "kol",  "kolose"           -> 50;
            case "1-tes", "1-tesalonika"    -> 51;
            case "2-tes", "2-tesalonika"    -> 52;
            case "1-tim", "1-timotius"      -> 53;
            case "2-tim", "2-timotius"      -> 54;
            case "tit",  "titus"            -> 55;
            case "flm",  "filemon"          -> 56;
            case "ibr",  "ibrani"           -> 57;
            case "yak",  "yakobus"          -> 58;
            case "1-ptr", "1-petrus"        -> 59;
            case "2-ptr", "2-petrus"        -> 60;
            case "1-yoh", "1-yohanes"       -> 61;
            case "2-yoh", "2-yohanes"       -> 62;
            case "3-yoh", "3-yohanes"       -> 63;
            case "yud",  "yudas"            -> 64;
            case "why",  "wahyu"            -> 65;
            default -> null;
        };
    }

    /**
     * Mapping nomor Book (0-65) → nama kitab Indonesia.
     */
    public String getNamaKitab(int bookNumber) {
        String[] names = {
            "Kejadian", "Keluaran", "Imamat", "Bilangan", "Ulangan",
            "Yosua", "Hakim-hakim", "Rut", "1 Samuel", "2 Samuel",
            "1 Raja-raja", "2 Raja-raja", "1 Tawarikh", "2 Tawarikh", "Ezra",
            "Nehemia", "Ester", "Ayub", "Mazmur", "Amsal",
            "Pengkhotbah", "Kidung Agung", "Yesaya", "Yeremia", "Ratapan",
            "Yehezkiel", "Daniel", "Hosea", "Yoel", "Amos",
            "Obaja", "Yunus", "Mikha", "Nahum", "Habakuk",
            "Zefanya", "Hagai", "Zakharia", "Maleakhi",
            "Matius", "Markus", "Lukas", "Yohanes", "Kisah Para Rasul",
            "Roma", "1 Korintus", "2 Korintus", "Galatia", "Efesus",
            "Filipi", "Kolose", "1 Tesalonika", "2 Tesalonika", "1 Timotius",
            "2 Timotius", "Titus", "Filemon", "Ibrani", "Yakobus",
            "1 Petrus", "2 Petrus", "1 Yohanes", "2 Yohanes", "3 Yohanes",
            "Yudas", "Wahyu"
        };
        if (bookNumber >= 0 && bookNumber < names.length) return names[bookNumber];
        return "Kitab " + bookNumber;
    }

    // ── QUERY METHODS ─────────────────────────────────────────────────────

    /**
     * Ambil single ayat.
     * Contoh: getAyat(42, 3, 16) → Yohanes 3:16
     */
    public AyatAlkitab getAyat(int book, int chapter, int verse) {
        String sql = "SELECT Book, Chapter, Versecount, verse FROM bible " +
                     "WHERE Book=? AND Chapter=? AND Versecount=?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, book);
            ps.setInt(2, chapter);
            ps.setInt(3, verse);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                AyatAlkitab a = map(rs);
                a.setNamaKitab(getNamaKitab(book));
                return a;
            }
        } catch (SQLException e) {
            System.err.println("[AyatAlkitabDAO] getAyat error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Ambil range ayat.
     * Contoh: getRangeAyat(18, 23, 1, 6) → Mazmur 23:1-6
     */
    public List<AyatAlkitab> getRangeAyat(int book, int chapter, int verseStart, int verseEnd) {
        List<AyatAlkitab> list = new ArrayList<>();
        String sql = "SELECT Book, Chapter, Versecount, verse FROM bible " +
                     "WHERE Book=? AND Chapter=? AND Versecount BETWEEN ? AND ? " +
                     "ORDER BY Versecount";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, book);
            ps.setInt(2, chapter);
            ps.setInt(3, verseStart);
            ps.setInt(4, verseEnd);
            ResultSet rs = ps.executeQuery();
            String namaKitab = getNamaKitab(book);
            while (rs.next()) {
                AyatAlkitab a = map(rs);
                a.setNamaKitab(namaKitab);
                list.add(a);
            }
        } catch (SQLException e) {
            System.err.println("[AyatAlkitabDAO] getRangeAyat error: " + e.getMessage());
        }
        return list;
    }

    /**
     * Ambil seluruh satu pasal.
     */
    public List<AyatAlkitab> getPasal(int book, int chapter) {
        List<AyatAlkitab> list = new ArrayList<>();
        String sql = "SELECT Book, Chapter, Versecount, verse FROM bible " +
                     "WHERE Book=? AND Chapter=? ORDER BY Versecount";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, book);
            ps.setInt(2, chapter);
            ResultSet rs = ps.executeQuery();
            String namaKitab = getNamaKitab(book);
            while (rs.next()) {
                AyatAlkitab a = map(rs);
                a.setNamaKitab(namaKitab);
                list.add(a);
            }
        } catch (SQLException e) {
            System.err.println("[AyatAlkitabDAO] getPasal error: " + e.getMessage());
        }
        return list;
    }

    /**
     * Cari teks ayat yang mengandung kata tertentu.
     * Contoh: searchAyat("kasih") → semua ayat yang mengandung kata "kasih"
     */
    public List<AyatAlkitab> searchAyat(String keyword, int limit) {
        List<AyatAlkitab> list = new ArrayList<>();
        String sql = "SELECT Book, Chapter, Versecount, verse FROM bible " +
                     "WHERE verse LIKE ? ORDER BY Book, Chapter, Versecount LIMIT ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword + "%");
            ps.setInt(2, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                AyatAlkitab a = map(rs);
                a.setNamaKitab(getNamaKitab(a.getBook()));
                list.add(a);
            }
        } catch (SQLException e) {
            System.err.println("[AyatAlkitabDAO] searchAyat error: " + e.getMessage());
        }
        return list;
    }

    /**
     * Ambil ayat acak.
     */
    public AyatAlkitab getAyatAcak() {
        String sql = "SELECT Book, Chapter, Versecount, verse FROM bible " +
                     "ORDER BY RANDOM() LIMIT 1";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                AyatAlkitab a = map(rs);
                a.setNamaKitab(getNamaKitab(a.getBook()));
                return a;
            }
        } catch (SQLException e) {
            System.err.println("[AyatAlkitabDAO] getAyatAcak error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Cek apakah holybible.db bisa diakses.
     */
    public boolean isAvailable() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeQuery("SELECT 1 FROM bible LIMIT 1");
            return true;
        } catch (SQLException e) {
            System.err.println("[AyatAlkitabDAO] DB tidak tersedia: " + e.getMessage());
            return false;
        }
    }

    // ── HELPER ────────────────────────────────────────────────────────────

    private AyatAlkitab map(ResultSet rs) throws SQLException {
        AyatAlkitab a = new AyatAlkitab();
        a.setBook(rs.getInt("Book"));
        a.setChapter(rs.getInt("Chapter"));
        a.setVersecount(rs.getInt("Versecount"));
        a.setVerse(rs.getString("verse"));
        return a;
    }
}
