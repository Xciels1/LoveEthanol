package com.chataja.model;

/**
 * Model untuk Ayat Alkitab dari holybible.db.
 * Kolom DB: Book (int 0-65), Chapter (int), Versecount (int), verse (text)
 */
public class AyatAlkitab {

    private int    book;        // nomor kitab 0-65
    private int    chapter;     // nomor pasal
    private int    versecount;  // nomor ayat
    private String verse;       // teks ayat
    private String namaKitab;   // nama kitab Indonesia (join/lookup)

    public AyatAlkitab() {}

    public AyatAlkitab(int book, int chapter, int versecount, String verse) {
        this.book       = book;
        this.chapter    = chapter;
        this.versecount = versecount;
        this.verse      = verse;
    }

    /** Format tampilan untuk chatbot — single ayat */
    public String tampilkan() {
        String ref = (namaKitab != null ? namaKitab : "Kitab " + book)
                   + " " + chapter + ":" + versecount;
        return "📖 AYAT ALKITAB\n"
             + "─".repeat(35) + "\n\n"
             + "🔖 " + ref + "\n\n"
             + "\"" + verse.trim() + "\"\n\n"
             + "— Alkitab Terjemahan Baru (TB)";
    }

    // ── Getters & Setters ─────────────────────────────────────────────────
    public int    getBook()       { return book; }
    public void   setBook(int v)  { this.book = v; }

    public int    getChapter()       { return chapter; }
    public void   setChapter(int v)  { this.chapter = v; }

    public int    getVersecount()       { return versecount; }
    public void   setVersecount(int v)  { this.versecount = v; }

    public String getVerse()       { return verse; }
    public void   setVerse(String v) { this.verse = v; }

    public String getNamaKitab()       { return namaKitab; }
    public void   setNamaKitab(String v) { this.namaKitab = v; }
}
