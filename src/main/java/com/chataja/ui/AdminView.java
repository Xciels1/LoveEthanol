package com.chataja.ui;

import com.chataja.dao.*;
import com.chataja.model.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Panel manajemen data untuk Admin.
 * Desain: dark theme sesuai Figma – layout section dengan kartu.
 */
public class AdminView extends VBox {

    private User currentUser;
    private final JadwalIbadahDAO jadwalIbadahDAO = new JadwalIbadahDAO();
    private final JadwalTugasDAO  jadwalTugasDAO  = new JadwalTugasDAO();
    private final LokasiDAO       lokasiDAO       = new LokasiDAO();
    private final UserDAO         userDAO         = new UserDAO();

    // Two separate panes, switched by nav
    private VBox paneIbadah;
    private VBox paneTugas;
    private StackPane contentStack;

    // Lists displayed in panes
    private VBox listIbadahBox;
    private VBox listTugasBox;

    // Colors
    private static final String BG      = "#3B3B3B";
    private static final String CARD    = "#484848";
    private static final String INPUT   = "#5C5C5C";
    private static final String ACCENT  = "#5B8DEF";
    private static final String DANGER  = "#E74C3C";
    private static final String DIVIDER = "#666666";

    public AdminView(User user) {
        this.currentUser = user;
        buildUI();
        setVisible(false);
        setManaged(false);
    }

    public void setUser(User user) { this.currentUser = user; }

    public void showJadwalIbadah() {
        paneIbadah.setVisible(true);  paneIbadah.setManaged(true);
        paneTugas.setVisible(false);  paneTugas.setManaged(false);
    }

    public void showJadwalTugas() {
        paneTugas.setVisible(true);   paneTugas.setManaged(true);
        paneIbadah.setVisible(false); paneIbadah.setManaged(false);
    }

    public void refresh() {
        refreshListIbadah();
        refreshListTugas();
    }

    private void buildUI() {
        setStyle("-fx-background-color: " + BG + ";");
        VBox.setVgrow(this, Priority.ALWAYS);

        contentStack = new StackPane();
        VBox.setVgrow(contentStack, Priority.ALWAYS);

        paneIbadah = buildJadwalIbadahPane();
        paneTugas  = buildJadwalTugasPane();

        // Start with ibadah visible
        paneTugas.setVisible(false); paneTugas.setManaged(false);

        contentStack.getChildren().addAll(paneIbadah, paneTugas);
        getChildren().add(contentStack);

        refreshListIbadah();
        refreshListTugas();
    }

    // ════════════════════════════════════════════════════════════════════
    //  PANE: JADWAL IBADAH
    // ════════════════════════════════════════════════════════════════════

    private VBox buildJadwalIbadahPane() {
        VBox pane = new VBox(0);
        pane.setStyle("-fx-background-color: " + BG + ";");
        VBox.setVgrow(pane, Priority.ALWAYS);

        // Header
        HBox header = ChatView.buildPageHeader("Kelola Jadwal Ibadah");
        VBox.setMargin(header, new Insets(12, 12, 0, 12));

        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: " + BG + "; -fx-background: " + BG + "; -fx-border-width: 0;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        VBox scrollContent = new VBox(16);
        scrollContent.setPadding(new Insets(16, 16, 16, 16));
        scrollContent.setStyle("-fx-background-color: " + BG + ";");

        // ── Tambah form ──
        scrollContent.getChildren().add(sectionTitle("Tambah Jadwal Ibadah"));

        VBox formCard = buildIbadahFormCard();
        scrollContent.getChildren().add(formCard);

        // ── Daftar ──
        scrollContent.getChildren().add(sectionTitle("Daftar Ibadah"));

        listIbadahBox = new VBox(8);
        listIbadahBox.setStyle("-fx-background-color: " + CARD + "; -fx-background-radius: 10; -fx-padding: 8;");
        scrollContent.getChildren().add(listIbadahBox);

        scroll.setContent(scrollContent);
        pane.getChildren().addAll(header, scroll);
        return pane;
    }

    private VBox buildIbadahFormCard() {
        TextField fNama  = styledField("Nama Ibadah");
        TextField fLokasi = styledField("Lokasi Ibadah");
        TextField fHari  = styledSmallField("DD");
        TextField fBulan = styledSmallField("DD");
        TextField fTahun = styledSmallField("YYYY"); fTahun.setPrefWidth(60);
        TextField fJam   = styledSmallField("00");
        TextField fMenit = styledSmallField("00");

        // Pre-fill date
        LocalDate today = LocalDate.now();
        fHari.setText(String.format("%02d", today.getDayOfMonth()));
        fBulan.setText(String.format("%02d", today.getMonthValue()));
        fTahun.setText(String.valueOf(today.getYear()));
        fJam.setText("07"); fMenit.setText("00");

        // Layout
        Label lblTgl = cardLabel("Tanggal Ibadah");
        HBox tglRow = new HBox(6, fHari, fBulan, fTahun);
        tglRow.setAlignment(Pos.CENTER_LEFT);

        Label lblWkt = cardLabel("Waktu Ibadah");
        HBox wktRow = new HBox(6, fJam, new Label(":") {{
            setTextFill(Color.WHITE); setFont(Font.font("System", FontWeight.BOLD, 14));
        }}, fMenit, new Label("WIB") {{
            setTextFill(Color.WHITE);
        }});
        wktRow.setAlignment(Pos.CENTER_LEFT);

        GridPane grid = new GridPane();
        grid.setHgap(16); grid.setVgap(12);
        ColumnConstraints c0 = new ColumnConstraints(); c0.setHgrow(Priority.ALWAYS);
        ColumnConstraints c1 = new ColumnConstraints(180);
        grid.getColumnConstraints().addAll(c0, c1);

        grid.add(cardLabel("Nama Ibadah"), 0, 0);
        grid.add(fNama, 0, 1);
        grid.add(lblTgl, 1, 0);
        grid.add(tglRow, 1, 1);
        grid.add(cardLabel("Lokasi Ibadah"), 0, 2);
        grid.add(fLokasi, 0, 3);
        grid.add(lblWkt, 1, 2);
        grid.add(wktRow, 1, 3);

        Button btnTambah = primaryButton("Tambahkan  Jadwal");
        HBox btnRow = new HBox(btnTambah);
        btnRow.setAlignment(Pos.CENTER);
        btnRow.setPadding(new Insets(6, 0, 0, 0));

        Label lblStatus = new Label();
        lblStatus.setTextFill(Color.WHITE);

        btnTambah.setOnAction(e -> {
            try {
                String nama   = fNama.getText().trim();
                String lokasi = fLokasi.getText().trim();
                if (nama.isEmpty()) { lblStatus.setText("⚠ Nama ibadah wajib diisi."); return; }

                LocalDate tgl = LocalDate.of(
                        Integer.parseInt(fTahun.getText().trim()),
                        Integer.parseInt(fBulan.getText().trim()),
                        Integer.parseInt(fHari.getText().trim()));
                LocalTime wkt = LocalTime.of(
                        Integer.parseInt(fJam.getText().trim()),
                        Integer.parseInt(fMenit.getText().trim()));

                // Cari atau buat lokasi
                String idLokasi = findOrCreateLokasi(lokasi);

                JadwalIbadah j = new JadwalIbadah();
                j.setNamaIbadah(nama);
                j.setTanggal(tgl);
                j.setWaktu(wkt);
                j.setIdLokasi(idLokasi);
                j.setIdUser(currentUser != null ? currentUser.getIdUser() : "");

                if (jadwalIbadahDAO.insert(j)) {
                    lblStatus.setText("✅ Jadwal berhasil disimpan!");
                    fNama.clear(); fLokasi.clear();
                    refreshListIbadah();
                }
            } catch (Exception ex) {
                lblStatus.setText("⚠ Periksa kembali isian tanggal dan waktu.");
            }
        });

        VBox card = new VBox(12, grid, btnRow, lblStatus);
        card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color: " + CARD + "; -fx-background-radius: 10;");
        return card;
    }

    private void refreshListIbadah() {
        if (listIbadahBox == null) return;
        listIbadahBox.getChildren().clear();
        List<JadwalIbadah> list = jadwalIbadahDAO.getAll();
        if (list.isEmpty()) {
            Label empty = new Label("Belum ada data jadwal ibadah.");
            empty.setTextFill(Color.web("#AAAAAA"));
            listIbadahBox.getChildren().add(empty);
            return;
        }
        for (JadwalIbadah item : list) {
            listIbadahBox.getChildren().add(buildIbadahRow(item));
        }
    }

    private HBox buildIbadahRow(JadwalIbadah item) {
        HBox row = new HBox(0);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 14, 10, 14));
        row.setStyle("-fx-background-color: #525252; -fx-background-radius: 8;");

        Label lNama = rowLabel(item.getNamaIbadah(), true);
        HBox.setHgrow(lNama, Priority.ALWAYS);

        Label lLokasi = rowLabel(item.getNamaLokasi() != null ? item.getNamaLokasi() : "-", false);
        lLokasi.setPrefWidth(160);

        Label lWkt  = rowLabel(item.getWaktuStr(), false);  lWkt.setPrefWidth(90);
        Label lTgl  = rowLabel(item.getTanggalStr(), false); lTgl.setPrefWidth(100);

        Button btnEdit  = actionBtn("✎ Edit", ACCENT);
        Button btnHapus = actionBtn("🗑 Hapus", DANGER);

        btnEdit.setOnAction(e -> showEditIbadahDialog(item));
        btnHapus.setOnAction(e -> {
            if (confirmDelete()) {
                jadwalIbadahDAO.delete(item.getIdJadwal());
                refreshListIbadah();
            }
        });

        row.getChildren().addAll(
                lNama, divider(), lLokasi, divider(), lWkt, divider(), lTgl,
                new Region() {{ HBox.setHgrow(this, Priority.ALWAYS); }},
                btnEdit, spacer(6), btnHapus
        );
        return row;
    }

    private void showEditIbadahDialog(JadwalIbadah item) {
        Dialog<JadwalIbadah> dlg = new Dialog<>();
        dlg.setTitle("Edit Jadwal Ibadah");
        TextField fNama   = new TextField(item.getNamaIbadah());
        DatePicker fTgl   = new DatePicker(item.getTanggal());
        TextField fWaktu  = new TextField(item.getWaktu() != null
                ? item.getWaktu().toString().substring(0, 5) : "");
        ComboBox<String> fLokasi = new ComboBox<>();
        refreshLokasiCombo(fLokasi);
        if (item.getNamaLokasi() != null)
            fLokasi.setValue(item.getIdLokasi() + " | " + item.getNamaLokasi());

        GridPane grid = dialogGrid();
        grid.add(dlgLabel("Nama Ibadah"), 0, 0);  grid.add(fNama, 1, 0);
        grid.add(dlgLabel("Tanggal"), 0, 1);       grid.add(fTgl, 1, 1);
        grid.add(dlgLabel("Waktu (HH:MM)"), 0, 2); grid.add(fWaktu, 1, 2);
        grid.add(dlgLabel("Lokasi"), 0, 3);         grid.add(fLokasi, 1, 3);

        dlg.getDialogPane().setContent(grid);
        dlg.getDialogPane().setPrefWidth(420);
        ButtonType ok = new ButtonType("Simpan", ButtonBar.ButtonData.OK_DONE);
        dlg.getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);
        dlg.setResultConverter(b -> {
            if (b == ok) {
                item.setNamaIbadah(fNama.getText().trim());
                item.setTanggal(fTgl.getValue());
                try { item.setWaktu(LocalTime.parse(fWaktu.getText().trim())); } catch (Exception ignored) {}
                if (fLokasi.getValue() != null) item.setIdLokasi(fLokasi.getValue().split("\\|")[0].trim());
                return item;
            }
            return null;
        });
        dlg.showAndWait().ifPresent(j -> { jadwalIbadahDAO.update(j); refreshListIbadah(); });
    }

    // ════════════════════════════════════════════════════════════════════
    //  PANE: JADWAL TUGAS
    // ════════════════════════════════════════════════════════════════════

    private VBox buildJadwalTugasPane() {
        VBox pane = new VBox(0);
        pane.setStyle("-fx-background-color: " + BG + ";");
        VBox.setVgrow(pane, Priority.ALWAYS);

        HBox header = ChatView.buildPageHeader("Kelola Jadwal Tugas");
        VBox.setMargin(header, new Insets(12, 12, 0, 12));

        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: " + BG + "; -fx-background: " + BG + "; -fx-border-width: 0;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        VBox scrollContent = new VBox(16);
        scrollContent.setPadding(new Insets(16, 16, 16, 16));
        scrollContent.setStyle("-fx-background-color: " + BG + ";");

        scrollContent.getChildren().add(sectionTitle("Tambah Tugas"));
        scrollContent.getChildren().add(buildTugasFormCard());
        scrollContent.getChildren().add(sectionTitle("Daftar Tugas"));

        listTugasBox = new VBox(8);
        listTugasBox.setStyle("-fx-background-color: " + CARD + "; -fx-background-radius: 10; -fx-padding: 8;");
        scrollContent.getChildren().add(listTugasBox);

        scroll.setContent(scrollContent);
        pane.getChildren().addAll(header, scroll);
        return pane;
    }

    private VBox buildTugasFormCard() {
        TextField fIdMajelis = styledField("Id Majelis");
        TextField fTugas     = styledField("Tugas");
        TextField fHari      = styledSmallField("DD");
        TextField fBulan     = styledSmallField("DD");
        TextField fTahun     = styledSmallField("YYYY"); fTahun.setPrefWidth(60);
        TextField fJam       = styledSmallField("00");
        TextField fMenit     = styledSmallField("00");

        LocalDate today = LocalDate.now();
        fHari.setText(String.format("%02d", today.getDayOfMonth()));
        fBulan.setText(String.format("%02d", today.getMonthValue()));
        fTahun.setText(String.valueOf(today.getYear()));
        fJam.setText("09"); fMenit.setText("00");

        HBox tglRow = new HBox(6, fHari, fBulan, fTahun);
        tglRow.setAlignment(Pos.CENTER_LEFT);
        HBox wktRow = new HBox(6, fJam, new Label(":") {{
            setTextFill(Color.WHITE); setFont(Font.font("System", FontWeight.BOLD, 14));
        }}, fMenit, new Label("WIB") {{ setTextFill(Color.WHITE); }});
        wktRow.setAlignment(Pos.CENTER_LEFT);

        GridPane grid = new GridPane();
        grid.setHgap(16); grid.setVgap(12);
        ColumnConstraints c0 = new ColumnConstraints(); c0.setHgrow(Priority.ALWAYS);
        ColumnConstraints c1 = new ColumnConstraints(200);
        grid.getColumnConstraints().addAll(c0, c1);

        grid.add(cardLabel("Id Majelis"), 0, 0);
        grid.add(fIdMajelis, 0, 1);
        grid.add(cardLabel("Tanggal Tugas"), 1, 0);
        grid.add(tglRow, 1, 1);
        grid.add(cardLabel("Tugas"), 0, 2);
        grid.add(fTugas, 0, 3);
        grid.add(cardLabel("Waktu Ibadah"), 1, 2);
        grid.add(wktRow, 1, 3);
        GridPane.setColumnSpan(fTugas, 2);
        GridPane.setColumnSpan(cardLabel("Tugas"), 2);

        Button btnTambah = primaryButton("Tambahkan  Tugas");
        HBox btnRow = new HBox(btnTambah);
        btnRow.setAlignment(Pos.CENTER);

        Label lblStatus = new Label();
        lblStatus.setTextFill(Color.WHITE);

        btnTambah.setOnAction(e -> {
            try {
                String idMajelis = fIdMajelis.getText().trim();
                String tugas     = fTugas.getText().trim();
                if (tugas.isEmpty()) { lblStatus.setText("⚠ Deskripsi tugas wajib diisi."); return; }

                LocalDate tgl = LocalDate.of(
                        Integer.parseInt(fTahun.getText().trim()),
                        Integer.parseInt(fBulan.getText().trim()),
                        Integer.parseInt(fHari.getText().trim()));
                LocalTime wkt = LocalTime.of(
                        Integer.parseInt(fJam.getText().trim()),
                        Integer.parseInt(fMenit.getText().trim()));

                JadwalTugas jt = new JadwalTugas();
                jt.setIdUser(idMajelis);
                jt.setTugas(tugas);
                jt.setTanggal(tgl);
                jt.setWaktuIbadah(wkt);

                if (jadwalTugasDAO.insert(jt)) {
                    lblStatus.setText("✅ Tugas berhasil disimpan!");
                    fIdMajelis.clear(); fTugas.clear();
                    refreshListTugas();
                }
            } catch (Exception ex) {
                lblStatus.setText("⚠ Periksa kembali isian tanggal dan waktu.");
            }
        });

        VBox card = new VBox(12, grid, btnRow, lblStatus);
        card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color: " + CARD + "; -fx-background-radius: 10;");
        return card;
    }

    private void refreshListTugas() {
        if (listTugasBox == null) return;
        listTugasBox.getChildren().clear();
        List<JadwalTugas> list = jadwalTugasDAO.getAll();
        if (list.isEmpty()) {
            Label empty = new Label("Belum ada data jadwal tugas.");
            empty.setTextFill(Color.web("#AAAAAA"));
            listTugasBox.getChildren().add(empty);
            return;
        }
        for (JadwalTugas item : list) {
            listTugasBox.getChildren().add(buildTugasRow(item));
        }
    }

    private HBox buildTugasRow(JadwalTugas item) {
        HBox row = new HBox(0);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 14, 10, 14));
        row.setStyle("-fx-background-color: #525252; -fx-background-radius: 8;");

        String nama = item.getNamaMajelis() != null ? item.getNamaMajelis() : item.getIdUser();
        Label lId    = rowLabel(nama, true);       lId.setPrefWidth(100);
        Label lTugas = rowLabel(item.getTugas(), false);
        HBox.setHgrow(lTugas, Priority.ALWAYS);
        Label lWkt  = rowLabel(item.getWaktuStr(), false);  lWkt.setPrefWidth(90);
        Label lTgl  = rowLabel(item.getTanggalStr(), false); lTgl.setPrefWidth(100);

        Button btnEdit  = actionBtn("✎ Edit", ACCENT);
        Button btnHapus = actionBtn("🗑 Hapus", DANGER);

        btnEdit.setOnAction(e -> showEditTugasDialog(item));
        btnHapus.setOnAction(e -> {
            if (confirmDelete()) { jadwalTugasDAO.delete(item.getIdTugas()); refreshListTugas(); }
        });

        row.getChildren().addAll(
                lId, divider(), lTugas, divider(), lWkt, divider(), lTgl,
                new Region() {{ HBox.setHgrow(this, Priority.ALWAYS); }},
                btnEdit, spacer(6), btnHapus
        );
        return row;
    }

    private void showEditTugasDialog(JadwalTugas item) {
        Dialog<JadwalTugas> dlg = new Dialog<>();
        dlg.setTitle("Edit Jadwal Tugas");
        ComboBox<String> fMajelis = new ComboBox<>();
        refreshMajelisCombo(fMajelis);
        TextField fTugas = new TextField(item.getTugas());
        DatePicker fTgl  = new DatePicker(item.getTanggal());
        TextField fWaktu = new TextField(item.getWaktuStr().replace(" WIB", "").equals("-") ? "" : item.getWaktuStr().replace(" WIB", ""));

        GridPane grid = dialogGrid();
        grid.add(dlgLabel("Majelis"), 0, 0);       grid.add(fMajelis, 1, 0);
        grid.add(dlgLabel("Tugas"), 0, 1);          grid.add(fTugas, 1, 1);
        grid.add(dlgLabel("Tanggal"), 0, 2);        grid.add(fTgl, 1, 2);
        grid.add(dlgLabel("Waktu (HH:MM)"), 0, 3); grid.add(fWaktu, 1, 3);

        dlg.getDialogPane().setContent(grid);
        dlg.getDialogPane().setPrefWidth(420);
        ButtonType ok = new ButtonType("Simpan", ButtonBar.ButtonData.OK_DONE);
        dlg.getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);
        dlg.setResultConverter(b -> {
            if (b == ok) {
                item.setTugas(fTugas.getText().trim());
                item.setTanggal(fTgl.getValue());
                if (fMajelis.getValue() != null) item.setIdUser(fMajelis.getValue().split("\\|")[0].trim());
                try { item.setWaktuIbadah(LocalTime.parse(fWaktu.getText().trim())); } catch (Exception ignored) {}
                return item;
            }
            return null;
        });
        dlg.showAndWait().ifPresent(jt -> { jadwalTugasDAO.update(jt); refreshListTugas(); });
    }

    // ════════════════════════════════════════════════════════════════════
    //  HELPERS
    // ════════════════════════════════════════════════════════════════════

    private String findOrCreateLokasi(String namaLokasi) {
        if (namaLokasi == null || namaLokasi.isEmpty()) return "";
        List<Lokasi> lokasis = lokasiDAO.getAll();
        for (Lokasi l : lokasis) {
            if (l.getNamaTempat().equalsIgnoreCase(namaLokasi)) return l.getIdLokasi();
        }
        Lokasi newLok = new Lokasi();
        newLok.setNamaTempat(namaLokasi);
        newLok.setAlamat(namaLokasi);
        newLok.setKontak("");
        newLok.setIdUser(currentUser != null ? currentUser.getIdUser() : "");
        lokasiDAO.insert(newLok);
        List<Lokasi> updated = lokasiDAO.getAll();
        for (Lokasi l : updated) {
            if (l.getNamaTempat().equalsIgnoreCase(namaLokasi)) return l.getIdLokasi();
        }
        return "";
    }

    private void refreshLokasiCombo(ComboBox<String> combo) {
        combo.getItems().clear();
        lokasiDAO.getAll().forEach(l -> combo.getItems().add(l.getIdLokasi() + " | " + l.getNamaTempat()));
    }

    private void refreshMajelisCombo(ComboBox<String> combo) {
        combo.getItems().clear();
        userDAO.getAllMajelis().forEach(u -> combo.getItems().add(u.getIdUser() + " | " + u.getNama()));
    }

    private boolean confirmDelete() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Hapus data ini?", ButtonType.OK, ButtonType.CANCEL);
        alert.setTitle("Konfirmasi Hapus");
        alert.setHeaderText(null);
        return alert.showAndWait().filter(r -> r == ButtonType.OK).isPresent();
    }

    // ── UI component builders ───────────────────────────────────────

    private Text sectionTitle(String text) {
        Text t = new Text(text);
        t.setFill(Color.WHITE);
        t.setFont(Font.font("System", FontWeight.BOLD, 18));
        return t;
    }

    private Label cardLabel(String text) {
        Label l = new Label(text);
        l.setTextFill(Color.web("#CCCCCC"));
        l.setFont(Font.font("System", 12));
        return l;
    }

    private Label rowLabel(String text, boolean bold) {
        Label l = new Label(text);
        l.setTextFill(Color.WHITE);
        l.setFont(Font.font("System", bold ? FontWeight.BOLD : FontWeight.NORMAL, 13));
        l.setPadding(new Insets(0, 8, 0, 8));
        return l;
    }

    private TextField styledField(String prompt) {
        TextField f = new TextField();
        f.setPromptText(prompt);
        f.setStyle("-fx-background-color: " + INPUT + "; -fx-text-fill: white; " +
                "-fx-prompt-text-fill: rgba(255,255,255,0.4); -fx-background-radius: 8; " +
                "-fx-border-width: 0; -fx-padding: 8 12 8 12; -fx-font-size: 13;");
        f.setMaxWidth(Double.MAX_VALUE);
        return f;
    }

    private TextField styledSmallField(String prompt) {
        TextField f = styledField(prompt);
        f.setPrefWidth(52);
        f.setMaxWidth(52);
        f.setAlignment(Pos.CENTER);
        return f;
    }

    private Button primaryButton(String text) {
        Button btn = new Button(text);
        btn.setFont(Font.font("System", FontWeight.BOLD, 13));
        btn.setPadding(new Insets(10, 40, 10, 40));
        String style = "-fx-background-color: " + ACCENT + "; -fx-text-fill: white; " +
                "-fx-background-radius: 22; -fx-cursor: hand; -fx-border-width: 0;";
        String hover = "-fx-background-color: #3D6FD4; -fx-text-fill: white; " +
                "-fx-background-radius: 22; -fx-cursor: hand; -fx-border-width: 0;";
        btn.setStyle(style);
        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited(e -> btn.setStyle(style));
        return btn;
    }

    private Button actionBtn(String text, String color) {
        Button btn = new Button(text);
        btn.setFont(Font.font("System", 12));
        btn.setPadding(new Insets(5, 12, 5, 12));
        String style = "-fx-background-color: " + color + "; -fx-text-fill: white; " +
                "-fx-background-radius: 6; -fx-cursor: hand; -fx-border-width: 0;";
        btn.setStyle(style);
        return btn;
    }

    private Region divider() {
        Region r = new Region();
        r.setPrefWidth(1); r.setPrefHeight(20);
        r.setStyle("-fx-background-color: " + DIVIDER + ";");
        r.setPadding(new Insets(0, 0, 0, 0));
        VBox.setMargin(r, new Insets(0, 4, 0, 4));
        return r;
    }

    private Region spacer(double w) {
        Region r = new Region(); r.setPrefWidth(w);
        return r;
    }

    private Label dlgLabel(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("System", FontWeight.BOLD, 12));
        l.setTextFill(Color.web("#4A5568"));
        return l;
    }

    private GridPane dialogGrid() {
        GridPane g = new GridPane();
        g.setHgap(12); g.setVgap(10);
        ColumnConstraints c0 = new ColumnConstraints(130);
        ColumnConstraints c1 = new ColumnConstraints(); c1.setHgrow(Priority.ALWAYS);
        g.getColumnConstraints().addAll(c0, c1);
        return g;
    }
}
