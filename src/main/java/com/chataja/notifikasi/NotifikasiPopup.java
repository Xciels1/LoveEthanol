package com.chataja.notifikasi;

import com.chataja.model.JadwalIbadah;
import com.chataja.model.JadwalTugas;
import com.chataja.ui.ChatAjaApp;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * NotifikasiPopup – popup notifikasi jadwal mendatang.
 *
 * Desain:
 *  • Header biru dengan ikon 🔔 dan jumlah total notifikasi
 *  • Kartu per item, warna badge sesuai kedekatan hari:
 *      HARI INI  → merah (#E74C3C)
 *      BESOK     → oranye (#E67E22)
 *      2 hari    → kuning (#F1C40F)
 *      3 hari    → hijau  (#27AE60)
 *  • Dua seksi: Jadwal Ibadah dan Jadwal Tugas (jika ada)
 *  • Tombol "Tutup" di kanan bawah
 */
public class NotifikasiPopup {

    // ── Warna (konsisten dengan ChatAjaApp) ──────────────────────────
    private static final String C_HEADER   = ChatAjaApp.COLOR_SIDEBAR;   // #6B9EC4
    private static final String C_BG       = ChatAjaApp.COLOR_BG;        // #3B3B3B
    private static final String C_CARD     = ChatAjaApp.COLOR_CARD;      // #484848
    private static final String C_CARD2    = ChatAjaApp.COLOR_CARD2;     // #525252
    private static final String C_ACCENT   = ChatAjaApp.COLOR_ACCENT;    // #5B8DEF
    private static final String C_WHITE    = ChatAjaApp.COLOR_WHITE;
    private static final String C_MUTED    = ChatAjaApp.COLOR_TEXT_MUTED;// #AAAAAA
    private static final String C_DANGER   = ChatAjaApp.COLOR_DANGER;    // #E74C3C
    private static final String C_SUCCESS  = ChatAjaApp.COLOR_SUCCESS;   // #27AE60

    private static final String C_ORANGE   = "#E67E22";
    private static final String C_YELLOW   = "#F39C12";

    // ── Data ─────────────────────────────────────────────────────────
    private final List<JadwalIbadah> jadwalIbadah;
    private final List<JadwalTugas>  jadwalTugas;
    private final LocalDate          today;
    private final String             sapaan;   // null = belum login

    private static final DateTimeFormatter FMT_TANGGAL =
            DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy", new Locale("id", "ID"));

    // ── Constructor ──────────────────────────────────────────────────

    public NotifikasiPopup(List<JadwalIbadah> jadwalIbadah,
                           List<JadwalTugas>  jadwalTugas,
                           LocalDate          today,
                           String             sapaan) {
        this.jadwalIbadah = jadwalIbadah;
        this.jadwalTugas  = jadwalTugas;
        this.today        = today;
        this.sapaan       = sapaan;
    }

    // ── Public ───────────────────────────────────────────────────────

    public void tampilkan() {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setResizable(false);

        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: " + C_BG + "; -fx-background-radius: 12;");

        root.getChildren().addAll(
                buildHeader(),
                buildBody(stage)
        );

        Scene scene = new Scene(root, 430, 0); // height auto
        scene.setFill(Color.TRANSPARENT);
        stage.setScene(scene);

        // Hitung tinggi dan posisikan di tengah
        stage.show();
        double targetH = Math.min(root.prefHeight(430), 540);
        stage.setHeight(targetH);
        stage.centerOnScreen();
    }

    // ── Builder: Header ──────────────────────────────────────────────

    private VBox buildHeader() {
        VBox header = new VBox(6);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(20, 24, 18, 24));
        header.setStyle("-fx-background-color: " + C_HEADER + "; -fx-background-radius: 12 12 0 0;");

        // Baris atas: ikon + judul
        HBox titleRow = new HBox(12);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        // Badge ikon 🔔 dalam lingkaran putih
        StackPane bellWrap = new StackPane();
        bellWrap.setMinSize(40, 40);
        bellWrap.setMaxSize(40, 40);
        bellWrap.setStyle("-fx-background-color: rgba(255,255,255,0.25); -fx-background-radius: 50;");
        Label bell = new Label("🔔");
        bell.setFont(Font.font(18));
        bellWrap.getChildren().add(bell);

        VBox titleMeta = new VBox(2);
        Label titleLbl = new Label("Pengingat Jadwal");
        titleLbl.setFont(Font.font("System", FontWeight.BOLD, 16));
        titleLbl.setTextFill(Color.WHITE);

        int total = jadwalIbadah.size() + jadwalTugas.size();
        String subText = sapaan != null
                ? "Hai, " + sapaan + "! Ada " + total + " jadwal dalam 3 hari ke depan."
                : "Ada " + total + " jadwal ibadah dalam 3 hari ke depan.";
        Label subLbl = new Label(subText);
        subLbl.setFont(Font.font("System", 12));
        subLbl.setTextFill(Color.web("rgba(255,255,255,0.80)"));
        subLbl.setWrapText(true);
        titleMeta.getChildren().addAll(titleLbl, subLbl);

        titleRow.getChildren().addAll(bellWrap, titleMeta);
        header.getChildren().add(titleRow);
        return header;
    }

    // ── Builder: Body ────────────────────────────────────────────────

    private VBox buildBody(Stage stage) {
        VBox body = new VBox(0);

        // Scroll area
        VBox listBox = new VBox(0);
        listBox.setPadding(new Insets(16, 16, 4, 16));
        listBox.setSpacing(0);

        // Seksi: Jadwal Ibadah
        if (!jadwalIbadah.isEmpty()) {
            listBox.getChildren().add(buildSeksiLabel("📅  Jadwal Ibadah", C_ACCENT));
            for (JadwalIbadah j : jadwalIbadah) {
                listBox.getChildren().add(buildKartuIbadah(j));
                listBox.getChildren().add(spacer(8));
            }
        }

        // Seksi: Jadwal Tugas (hanya majelis)
        if (!jadwalTugas.isEmpty()) {
            listBox.getChildren().add(spacer(4));
            listBox.getChildren().add(buildSeksiLabel("🛎️  Jadwal Tugas Saya", C_ORANGE));
            for (JadwalTugas jt : jadwalTugas) {
                listBox.getChildren().add(buildKartuTugas(jt));
                listBox.getChildren().add(spacer(8));
            }
        }

        ScrollPane scroll = new ScrollPane(listBox);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent; " +
                        "-fx-border-color: transparent;");
        scroll.setMaxHeight(380);

        // Tombol tutup
        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(12, 20, 16, 20));
        footer.setStyle("-fx-background-color: " + C_CARD + "; -fx-background-radius: 0 0 12 12;");

        Button btnTutup = new Button("Tutup  ✕");
        btnTutup.setFont(Font.font("System", FontWeight.BOLD, 13));
        btnTutup.setPadding(new Insets(9, 24, 9, 24));
        btnTutup.setStyle(
                "-fx-background-color: " + C_ACCENT + "; -fx-text-fill: white; " +
                "-fx-background-radius: 20; -fx-cursor: hand; -fx-border-width: 0;");
        btnTutup.setOnMouseEntered(e -> btnTutup.setStyle(
                "-fx-background-color: " + ChatAjaApp.COLOR_ACCENT_DARK + "; -fx-text-fill: white; " +
                "-fx-background-radius: 20; -fx-cursor: hand;"));
        btnTutup.setOnMouseExited(e -> btnTutup.setStyle(
                "-fx-background-color: " + C_ACCENT + "; -fx-text-fill: white; " +
                "-fx-background-radius: 20; -fx-cursor: hand;"));
        btnTutup.setOnAction(e -> stage.close());

        footer.getChildren().add(btnTutup);

        body.getChildren().addAll(scroll, footer);
        return body;
    }

    // ── Builder: Kartu Jadwal Ibadah ─────────────────────────────────

    private HBox buildKartuIbadah(JadwalIbadah j) {
        HBox kartu = new HBox(12);
        kartu.setAlignment(Pos.CENTER_LEFT);
        kartu.setPadding(new Insets(12, 14, 12, 14));
        kartu.setStyle("-fx-background-color: " + C_CARD2 + "; -fx-background-radius: 10;");

        // Badge hari
        StackPane badge = buildBadgeHari(j.getTanggal());

        // Info
        VBox info = new VBox(3);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label namaLbl = new Label(j.getNamaIbadah());
        namaLbl.setFont(Font.font("System", FontWeight.BOLD, 13));
        namaLbl.setTextFill(Color.WHITE);

        String tanggalStr = j.getTanggal() != null ? j.getTanggal().format(FMT_TANGGAL) : "-";
        Label tanggalLbl = new Label("📆 " + tanggalStr);
        tanggalLbl.setFont(Font.font("System", 11));
        tanggalLbl.setTextFill(Color.web(C_MUTED));

        HBox meta = new HBox(12);
        meta.setAlignment(Pos.CENTER_LEFT);
        Label waktuLbl = new Label("🕐 " + j.getWaktuStr());
        waktuLbl.setFont(Font.font("System", 11));
        waktuLbl.setTextFill(Color.web(C_MUTED));

        Label lokasiLbl = new Label("📍 " + (j.getNamaLokasi() != null ? j.getNamaLokasi() : "-"));
        lokasiLbl.setFont(Font.font("System", 11));
        lokasiLbl.setTextFill(Color.web(C_MUTED));

        meta.getChildren().addAll(waktuLbl, lokasiLbl);
        info.getChildren().addAll(namaLbl, tanggalLbl, meta);

        kartu.getChildren().addAll(badge, info);
        addHoverEffect(kartu);
        return kartu;
    }

    // ── Builder: Kartu Jadwal Tugas ──────────────────────────────────

    private HBox buildKartuTugas(JadwalTugas jt) {
        HBox kartu = new HBox(12);
        kartu.setAlignment(Pos.CENTER_LEFT);
        kartu.setPadding(new Insets(12, 14, 12, 14));
        kartu.setStyle("-fx-background-color: " + C_CARD2 + "; -fx-background-radius: 10;");

        // Badge hari
        StackPane badge = buildBadgeHari(jt.getTanggal());

        // Info
        VBox info = new VBox(3);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label tugasLbl = new Label(jt.getTugas());
        tugasLbl.setFont(Font.font("System", FontWeight.BOLD, 13));
        tugasLbl.setTextFill(Color.WHITE);
        tugasLbl.setWrapText(true);

        String tanggalStr = jt.getTanggal() != null ? jt.getTanggal().format(FMT_TANGGAL) : "-";
        Label tanggalLbl = new Label("📆 " + tanggalStr);
        tanggalLbl.setFont(Font.font("System", 11));
        tanggalLbl.setTextFill(Color.web(C_MUTED));

        Label waktuLbl = new Label("🕐 " + jt.getWaktuStr());
        waktuLbl.setFont(Font.font("System", 11));
        waktuLbl.setTextFill(Color.web(C_MUTED));

        info.getChildren().addAll(tugasLbl, tanggalLbl, waktuLbl);
        kartu.getChildren().addAll(badge, info);
        addHoverEffect(kartu);
        return kartu;
    }

    // ── Helper: Badge Hari ───────────────────────────────────────────

    /**
     * Badge di sebelah kiri kartu, menunjukkan jarak hari.
     * Warna:
     *   Hari ini  → merah
     *   Besok     → oranye
     *   2 hari    → kuning
     *   3 hari    → hijau
     */
    private StackPane buildBadgeHari(LocalDate tanggal) {
        long selisih = (tanggal != null) ? today.until(tanggal).getDays() : -1;

        String warna;
        String labelAtas;
        String labelBawah;

        if (selisih <= 0) {
            warna      = C_DANGER;
            labelAtas  = "HARI";
            labelBawah = "INI";
        } else if (selisih == 1) {
            warna      = C_ORANGE;
            labelAtas  = "BE-";
            labelBawah = "SOK";
        } else if (selisih == 2) {
            warna      = C_YELLOW;
            labelAtas  = "2";
            labelBawah = "HARI";
        } else {
            warna      = C_SUCCESS;
            labelAtas  = "3";
            labelBawah = "HARI";
        }

        StackPane wrap = new StackPane();
        wrap.setMinSize(46, 52);
        wrap.setMaxSize(46, 52);
        wrap.setStyle("-fx-background-color: " + warna + "; -fx-background-radius: 8;");

        VBox lbl = new VBox(0);
        lbl.setAlignment(Pos.CENTER);
        Label l1 = new Label(labelAtas);
        l1.setFont(Font.font("System", FontWeight.BOLD, 12));
        l1.setTextFill(Color.WHITE);
        Label l2 = new Label(labelBawah);
        l2.setFont(Font.font("System", FontWeight.BOLD, 12));
        l2.setTextFill(Color.WHITE);
        lbl.getChildren().addAll(l1, l2);

        wrap.getChildren().add(lbl);
        return wrap;
    }

    // ── Helper: Label Seksi ──────────────────────────────────────────

    private Label buildSeksiLabel(String teks, String warna) {
        Label lbl = new Label(teks);
        lbl.setFont(Font.font("System", FontWeight.BOLD, 12));
        lbl.setTextFill(Color.web(warna));
        lbl.setPadding(new Insets(0, 0, 8, 2));
        return lbl;
    }

    // ── Helper: Hover effect kartu ────────────────────────────────────

    private void addHoverEffect(HBox kartu) {
        String normal = "-fx-background-color: " + C_CARD2 + "; -fx-background-radius: 10;";
        String hover  = "-fx-background-color: #5a5a5a; -fx-background-radius: 10;";
        kartu.setOnMouseEntered(e -> kartu.setStyle(hover));
        kartu.setOnMouseExited(e  -> kartu.setStyle(normal));
    }

    // ── Helper: Spacer ────────────────────────────────────────────────

    private Region spacer(double tinggi) {
        Region r = new Region();
        r.setMinHeight(tinggi);
        r.setPrefHeight(tinggi);
        return r;
    }
}
