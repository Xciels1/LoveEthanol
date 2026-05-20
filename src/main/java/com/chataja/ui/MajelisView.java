package com.chataja.ui;

import com.chataja.chatbot.ChatBot;
import com.chataja.dao.PengumumanDAO;
import com.chataja.dao.RenunganDAO;
import com.chataja.model.Pengumuman;
import com.chataja.model.Renungan;
import com.chataja.model.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.List;

/**
 * Panel manajemen untuk Majelis Gereja.
 * Desain: dark theme sesuai Figma.
 */
public class MajelisView extends VBox {

    private User currentUser;
    private final ChatBot chatBot;
    private final PengumumanDAO pengumumanDAO = new PengumumanDAO();
    private final RenunganDAO   renunganDAO   = new RenunganDAO();

    private VBox panePengumuman;
    private VBox paneRenungan;

    private VBox listPengumumanBox;
    private VBox listRenunganBox;

    private static final String BG      = "#3B3B3B";
    private static final String CARD    = "#484848";
    private static final String INPUT   = "#5C5C5C";
    private static final String ACCENT  = "#5B8DEF";
    private static final String DANGER  = "#E74C3C";
    private static final String DIVIDER = "#666666";

    public MajelisView(User user, ChatBot chatBot) {
        this.currentUser = user;
        this.chatBot = chatBot;
        buildUI();
        setVisible(false);
        setManaged(false);
    }

    public void setUser(User user) { this.currentUser = user; }

    public void showPengumuman() {
        panePengumuman.setVisible(true);  panePengumuman.setManaged(true);
        paneRenungan.setVisible(false);   paneRenungan.setManaged(false);
    }

    public void showRenungan() {
        paneRenungan.setVisible(true);    paneRenungan.setManaged(true);
        panePengumuman.setVisible(false); panePengumuman.setManaged(false);
    }

    public void refresh() {
        refreshListPengumuman();
        refreshListRenungan();
    }

    private void buildUI() {
        setStyle("-fx-background-color: " + BG + ";");
        VBox.setVgrow(this, Priority.ALWAYS);

        StackPane contentStack = new StackPane();
        VBox.setVgrow(contentStack, Priority.ALWAYS);

        panePengumuman = buildPengumumanPane();
        paneRenungan   = buildRenunganPane();

        paneRenungan.setVisible(false);
        paneRenungan.setManaged(false);

        contentStack.getChildren().addAll(panePengumuman, paneRenungan);
        getChildren().add(contentStack);

        refreshListPengumuman();
        refreshListRenungan();
    }

    // ════════════════════════════════════════════════════════════════════
    //  PANE: PENGUMUMAN
    // ════════════════════════════════════════════════════════════════════

    private VBox buildPengumumanPane() {
        VBox pane = new VBox(0);
        pane.setStyle("-fx-background-color: " + BG + ";");
        VBox.setVgrow(pane, Priority.ALWAYS);

        HBox header = ChatView.buildPageHeader("Kelola Pengumuman");
        VBox.setMargin(header, new Insets(12, 12, 0, 12));

        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: " + BG + "; -fx-background: " + BG + "; -fx-border-width: 0;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        VBox content = new VBox(20);
        content.setPadding(new Insets(16, 16, 24, 16));
        content.setStyle("-fx-background-color: " + BG + ";");

        content.getChildren().add(sectionTitle("Tambah Pengumuman"));
        content.getChildren().add(buildPengumumanFormCard());
        content.getChildren().add(sectionTitle("Daftar Pengumuman"));

        listPengumumanBox = new VBox(8);
        listPengumumanBox.setStyle(
                "-fx-background-color: " + CARD + "; -fx-background-radius: 10; -fx-padding: 10;");
        content.getChildren().add(listPengumumanBox);

        scroll.setContent(content);
        pane.getChildren().addAll(header, scroll);
        return pane;
    }

    private VBox buildPengumumanFormCard() {
        TextField fJudul = styledField("judul");
        TextArea  fIsi   = styledTextArea("pengumuman");
        TextField fHari  = styledSmallField("DD");
        TextField fBulan = styledSmallField("MM");
        TextField fTahun = styledSmallField("YYYY"); fTahun.setPrefWidth(60);

        LocalDate today = LocalDate.now();
        fHari.setText(String.format("%02d", today.getDayOfMonth()));
        fBulan.setText(String.format("%02d", today.getMonthValue()));
        fTahun.setText(String.valueOf(today.getYear()));

        // ── Foto upload (real FileChooser) ───────────────────────────────
        final String[] selectedFotoPath = {null};

        // Preview image
        ImageView preview = new ImageView();
        preview.setFitWidth(140);
        preview.setFitHeight(80);
        preview.setPreserveRatio(true);
        preview.setSmooth(true);

        Label lblFotoNama = new Label("drop here !");
        lblFotoNama.setTextFill(Color.web("#AAAAAA"));
        lblFotoNama.setFont(Font.font("System", 11));
        lblFotoNama.setWrapText(true);

        Label dropIcon = new Label("📄");
        dropIcon.setFont(Font.font("System", 18));

        VBox dropZone = new VBox(4);
        dropZone.setAlignment(Pos.CENTER);
        dropZone.setPrefSize(140, 90);
        dropZone.setStyle("-fx-background-color: " + INPUT + "; -fx-background-radius: 8; " +
                "-fx-border-color: #888; -fx-border-style: dashed; -fx-border-radius: 8; " +
                "-fx-cursor: hand;");
        dropZone.getChildren().addAll(dropIcon, lblFotoNama);

        // Klik dropZone → buka file chooser
        dropZone.setOnMouseClicked(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Pilih Foto Pengumuman");
            fc.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Gambar", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"));
            File file = fc.showOpenDialog(getScene().getWindow());
            if (file != null) {
                selectedFotoPath[0] = file.getAbsolutePath();
                try {
                    Image img = new Image(file.toURI().toString(), 140, 80, true, true);
                    preview.setImage(img);
                    dropZone.getChildren().setAll(preview, new Label("✅ " + file.getName()) {{
                        setTextFill(Color.web("#AAAAAA"));
                        setFont(Font.font("System", 10));
                        setWrapText(true);
                    }});
                    dropZone.setStyle("-fx-background-color: " + INPUT + "; -fx-background-radius: 8; " +
                            "-fx-border-color: " + ACCENT + "; -fx-border-style: solid; " +
                            "-fx-border-radius: 8; -fx-cursor: hand;");
                } catch (Exception ex) {
                    lblFotoNama.setText("⚠ " + file.getName());
                }
            }
        });

        // Tombol hapus foto
        Button btnHapusFoto = new Button("✕ Hapus Foto");
        btnHapusFoto.setFont(Font.font("System", 11));
        btnHapusFoto.setPadding(new Insets(4, 10, 4, 10));
        btnHapusFoto.setStyle("-fx-background-color: " + DANGER + "; -fx-text-fill: white; " +
                "-fx-background-radius: 6; -fx-cursor: hand; -fx-border-width: 0;");
        btnHapusFoto.setVisible(false);
        btnHapusFoto.setManaged(false);
        btnHapusFoto.setOnAction(ev -> {
            selectedFotoPath[0] = null;
            preview.setImage(null);
            dropZone.getChildren().setAll(dropIcon, lblFotoNama);
            lblFotoNama.setText("drop here !");
            dropZone.setStyle("-fx-background-color: " + INPUT + "; -fx-background-radius: 8; " +
                    "-fx-border-color: #888; -fx-border-style: dashed; -fx-border-radius: 8; " +
                    "-fx-cursor: hand;");
            btnHapusFoto.setVisible(false);
            btnHapusFoto.setManaged(false);
        });

        // Tampilkan tombol hapus setelah foto dipilih
        dropZone.setOnMouseClicked(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Pilih Foto Pengumuman");
            fc.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Gambar", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"));
            File file = fc.showOpenDialog(getScene().getWindow());
            if (file != null) {
                selectedFotoPath[0] = file.getAbsolutePath();
                try {
                    Image img = new Image(file.toURI().toString(), 140, 80, true, true);
                    preview.setImage(img);
                    Label namaFile = new Label("✅ " + file.getName());
                    namaFile.setTextFill(Color.web("#AAAAAA"));
                    namaFile.setFont(Font.font("System", 10));
                    namaFile.setWrapText(true);
                    dropZone.getChildren().setAll(preview, namaFile);
                    dropZone.setStyle("-fx-background-color: " + INPUT + "; -fx-background-radius: 8; " +
                            "-fx-border-color: " + ACCENT + "; -fx-border-style: solid; " +
                            "-fx-border-radius: 8; -fx-cursor: hand;");
                    btnHapusFoto.setVisible(true);
                    btnHapusFoto.setManaged(true);
                } catch (Exception ex) {
                    lblFotoNama.setText("⚠ Gagal memuat: " + file.getName());
                }
            }
        });

        VBox fotoBox = new VBox(6);
        fotoBox.setAlignment(Pos.TOP_LEFT);
        fotoBox.getChildren().addAll(dropZone, btnHapusFoto);

        // ── Layout grid ──────────────────────────────────────────────────
        HBox tglRow = new HBox(6, fHari, fBulan, fTahun);
        tglRow.setAlignment(Pos.CENTER_LEFT);

        GridPane grid = new GridPane();
        grid.setHgap(16); grid.setVgap(12);
        ColumnConstraints c0 = new ColumnConstraints(); c0.setHgrow(Priority.ALWAYS);
        ColumnConstraints c1 = new ColumnConstraints(160);
        grid.getColumnConstraints().addAll(c0, c1);

        grid.add(cardLabel("Judul Pengumuman"), 0, 0);
        grid.add(fJudul, 0, 1);
        grid.add(cardLabel("Tanggal Pengumuman"), 1, 0);
        grid.add(tglRow, 1, 1);
        grid.add(cardLabel("Isi Pengumuman"), 0, 2);
        grid.add(cardLabel("Tambahkan Foto *(opsional)"), 1, 2);
        grid.add(fIsi, 0, 3);
        grid.add(fotoBox, 1, 3);

        Button btnTambah = primaryButton("Tambahkan  Pengumuman");
        HBox btnRow = new HBox(btnTambah);
        btnRow.setAlignment(Pos.CENTER);
        btnRow.setPadding(new Insets(4, 0, 0, 0));

        Label lblStatus = new Label();
        lblStatus.setTextFill(Color.WHITE);

        btnTambah.setOnAction(e -> {
            String judul = fJudul.getText().trim();
            String isi   = fIsi.getText().trim();
            if (judul.isEmpty() || isi.isEmpty()) {
                lblStatus.setText("⚠ Judul dan isi wajib diisi.");
                lblStatus.setTextFill(Color.web("#FFB347"));
                return;
            }
            try {
                LocalDate tgl = LocalDate.of(
                        Integer.parseInt(fTahun.getText().trim()),
                        Integer.parseInt(fBulan.getText().trim()),
                        Integer.parseInt(fHari.getText().trim()));

                // Salin foto ke uploads jika ada
                String savedPath = null;
                if (selectedFotoPath[0] != null) {
                    savedPath = copyFotoToUploads(selectedFotoPath[0], lblStatus);
                    if (savedPath == null) return; // gagal copy, pesan sudah ditampilkan
                }

                Pengumuman p = new Pengumuman();
                p.setJudul(judul);
                p.setIsi(isi);
                p.setTanggal(tgl);
                p.setIdUser(currentUser != null ? currentUser.getIdUser() : "");
                p.setFotoPath(savedPath);

                if (pengumumanDAO.insert(p)) {
                    lblStatus.setText("✅ Pengumuman berhasil disimpan!");
                    lblStatus.setTextFill(Color.web("#55EFC4"));
                    fJudul.clear(); fIsi.clear();

                    // Reset foto
                    selectedFotoPath[0] = null;
                    preview.setImage(null);
                    dropZone.getChildren().setAll(dropIcon, lblFotoNama);
                    lblFotoNama.setText("drop here !");
                    dropZone.setStyle("-fx-background-color: " + INPUT + "; -fx-background-radius: 8; " +
                            "-fx-border-color: #888; -fx-border-style: dashed; -fx-border-radius: 8; " +
                            "-fx-cursor: hand;");
                    btnHapusFoto.setVisible(false);
                    btnHapusFoto.setManaged(false);

                    refreshListPengumuman();
                }
            } catch (Exception ex) {
                lblStatus.setText("⚠ Periksa kembali isian tanggal.");
                lblStatus.setTextFill(Color.web("#FFB347"));
            }
        });

        VBox card = new VBox(12, grid, btnRow, lblStatus);
        card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color: " + CARD + "; -fx-background-radius: 10;");
        return card;
    }

    private void refreshListPengumuman() {
        if (listPengumumanBox == null) return;
        listPengumumanBox.getChildren().clear();
        List<Pengumuman> list = pengumumanDAO.getAll();
        if (list.isEmpty()) {
            Label empty = new Label("Belum ada pengumuman.");
            empty.setTextFill(Color.web("#AAAAAA"));
            listPengumumanBox.getChildren().add(empty);
            return;
        }
        for (Pengumuman item : list) {
            listPengumumanBox.getChildren().add(buildPengumumanRow(item));
        }
    }

    private HBox buildPengumumanRow(Pengumuman item) {
        HBox row = new HBox(0);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 14, 10, 14));
        row.setStyle("-fx-background-color: #525252; -fx-background-radius: 8;");
        row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: #5a5a5a; -fx-background-radius: 8;"));
        row.setOnMouseExited(e -> row.setStyle("-fx-background-color: #525252; -fx-background-radius: 8;"));

        Label lJudul = rowLabel(item.getJudul(), true); lJudul.setPrefWidth(160);
        String isiShort = item.getIsi() != null && item.getIsi().length() > 70
                ? item.getIsi().substring(0, 70) + " ..." : item.getIsi();
        Label lIsi  = rowLabel(isiShort != null ? isiShort : "-", false);
        HBox.setHgrow(lIsi, Priority.ALWAYS);
        Label lTgl  = rowLabel(item.getTanggalStr(), false); lTgl.setPrefWidth(100);

        // Indikator foto
        Label lFoto = rowLabel(item.hasFoto() ? "📷" : "—", false);
        lFoto.setPrefWidth(30);
        lFoto.setAlignment(Pos.CENTER);

        Button btnEdit  = actionBtn("✎ Edit",   ACCENT);
        Button btnHapus = actionBtn("🗑 Hapus", DANGER);

        btnEdit.setOnAction(e -> showEditPengumumanDialog(item));
        btnHapus.setOnAction(e -> {
            if (confirmDelete()) {
                pengumumanDAO.delete(item.getIdPengumuman());
                refreshListPengumuman();
            }
        });

        row.getChildren().addAll(
                lJudul, divider(), lIsi, divider(), lTgl, divider(), lFoto,
                new Region() {{ HBox.setHgrow(this, Priority.ALWAYS); }},
                btnEdit, spacer(6), btnHapus
        );
        return row;
    }

    private void showEditPengumumanDialog(Pengumuman item) {
        Dialog<Pengumuman> dlg = new Dialog<>();
        dlg.setTitle("Edit Pengumuman");

        TextField  fJudul = new TextField(item.getJudul());
        TextArea   fIsi   = new TextArea(item.getIsi());
        fIsi.setPrefRowCount(5); fIsi.setWrapText(true);
        DatePicker fTgl   = new DatePicker(item.getTanggal());

        // ── Foto section ─────────────────────────────────────────────────
        final String[] selectedFotoPath = {item.getFotoPath()};

        ImageView preview = new ImageView();
        preview.setFitWidth(160); preview.setFitHeight(90);
        preview.setPreserveRatio(true); preview.setSmooth(true);

        Label lblFotoNama = new Label();
        lblFotoNama.setFont(Font.font("System", 11));
        lblFotoNama.setTextFill(Color.web("#666666"));
        lblFotoNama.setWrapText(true);

        // Load foto lama jika ada
        if (item.hasFoto()) {
            File f = new File(item.getFotoPath());
            if (f.exists()) {
                try {
                    preview.setImage(new Image(f.toURI().toString(), 160, 90, true, true));
                    lblFotoNama.setText("📷 " + f.getName());
                } catch (Exception ignored) {
                    lblFotoNama.setText("⚠ Gagal memuat foto.");
                }
            } else {
                lblFotoNama.setText("⚠ File tidak ditemukan.");
            }
        } else {
            lblFotoNama.setText("Belum ada foto.");
        }

        Button btnGantiFoto = new Button("🔄 Ganti Foto");
        btnGantiFoto.setFont(Font.font("System", 11));
        btnGantiFoto.setPadding(new Insets(5, 12, 5, 12));
        btnGantiFoto.setStyle("-fx-background-color: #718096; -fx-text-fill: white; " +
                "-fx-background-radius: 6; -fx-cursor: hand;");

        Button btnHapusFoto = new Button("✕ Hapus Foto");
        btnHapusFoto.setFont(Font.font("System", 11));
        btnHapusFoto.setPadding(new Insets(5, 12, 5, 12));
        btnHapusFoto.setStyle("-fx-background-color: " + DANGER + "; -fx-text-fill: white; " +
                "-fx-background-radius: 6; -fx-cursor: hand;");

        btnGantiFoto.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Ganti Foto");
            fc.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Gambar", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"));
            File file = fc.showOpenDialog(dlg.getOwner());
            if (file != null) {
                selectedFotoPath[0] = file.getAbsolutePath();
                try {
                    preview.setImage(new Image(file.toURI().toString(), 160, 90, true, true));
                    lblFotoNama.setText("📷 " + file.getName() + " (baru)");
                } catch (Exception ignored) {}
            }
        });

        btnHapusFoto.setOnAction(e -> {
            selectedFotoPath[0] = null;
            preview.setImage(null);
            lblFotoNama.setText("Foto dihapus.");
        });

        HBox fotoButtons = new HBox(8, btnGantiFoto, btnHapusFoto);
        VBox fotoSection = new VBox(6, fotoButtons, preview, lblFotoNama);

        // ── Grid dialog ──────────────────────────────────────────────────
        GridPane grid = dialogGrid();
        grid.add(dlgLabel("Judul"), 0, 0);   grid.add(fJudul, 1, 0);
        grid.add(dlgLabel("Isi"), 0, 1);     grid.add(fIsi, 1, 1);
        grid.add(dlgLabel("Tanggal"), 0, 2); grid.add(fTgl, 1, 2);
        grid.add(dlgLabel("Foto"), 0, 3);    grid.add(fotoSection, 1, 3);

        dlg.getDialogPane().setContent(grid);
        dlg.getDialogPane().setPrefWidth(500);

        ButtonType ok = new ButtonType("Simpan", ButtonBar.ButtonData.OK_DONE);
        dlg.getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);

        dlg.setResultConverter(b -> {
            if (b == ok) {
                item.setJudul(fJudul.getText().trim());
                item.setIsi(fIsi.getText().trim());
                item.setTanggal(fTgl.getValue());

                String currentSaved = item.getFotoPath();
                String selected     = selectedFotoPath[0];

                if (selected == null) {
                    // Foto dihapus
                    item.setFotoPath(null);
                } else if (!selected.equals(currentSaved)) {
                    // Foto baru dipilih — salin ke uploads
                    Label dummy = new Label();
                    String copied = copyFotoToUploads(selected, dummy);
                    item.setFotoPath(copied);
                }
                // Sama dengan sebelumnya → tidak berubah
                return item;
            }
            return null;
        });

        dlg.showAndWait().ifPresent(p -> { pengumumanDAO.update(p); refreshListPengumuman(); });
    }

    // ════════════════════════════════════════════════════════════════════
    //  PANE: RENUNGAN (tidak berubah)
    // ════════════════════════════════════════════════════════════════════

    private VBox buildRenunganPane() {
        VBox pane = new VBox(0);
        pane.setStyle("-fx-background-color: " + BG + ";");
        VBox.setVgrow(pane, Priority.ALWAYS);

        HBox header = ChatView.buildPageHeader("Kelola Renungan");
        VBox.setMargin(header, new Insets(12, 12, 0, 12));

        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: " + BG + "; -fx-background: " + BG + "; -fx-border-width: 0;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        VBox content = new VBox(20);
        content.setPadding(new Insets(16, 16, 24, 16));
        content.setStyle("-fx-background-color: " + BG + ";");

        content.getChildren().add(sectionTitle("Tambah Renungan"));
        content.getChildren().add(buildRenunganFormCard());
        content.getChildren().add(sectionTitle("Daftar Renungan"));

        listRenunganBox = new VBox(8);
        listRenunganBox.setStyle(
                "-fx-background-color: " + CARD + "; -fx-background-radius: 10; -fx-padding: 10;");
        content.getChildren().add(listRenunganBox);

        scroll.setContent(content);
        pane.getChildren().addAll(header, scroll);
        return pane;
    }

    private VBox buildRenunganFormCard() {
        TextField fJudul = styledField("judul");
        TextArea  fIsi   = styledTextArea("Renungan");
        TextField fHari  = styledSmallField("DD");
        TextField fBulan = styledSmallField("MM");
        TextField fTahun = styledSmallField("YYYY"); fTahun.setPrefWidth(60);

        LocalDate today = LocalDate.now();
        fHari.setText(String.format("%02d", today.getDayOfMonth()));
        fBulan.setText(String.format("%02d", today.getMonthValue()));
        fTahun.setText(String.valueOf(today.getYear()));

        // Drop zone (visual only — renungan tidak butuh foto)
        VBox dropZone = new VBox(4);
        dropZone.setAlignment(Pos.CENTER);
        dropZone.setPrefSize(140, 90);
        dropZone.setStyle("-fx-background-color: " + INPUT + "; -fx-background-radius: 8; " +
                "-fx-border-color: #888; -fx-border-style: dashed; -fx-border-radius: 8;");
        Label dropText = new Label("drop here !"); dropText.setTextFill(Color.web("#AAAAAA")); dropText.setFont(Font.font("System", 11));
        Label dropIcon = new Label("📄"); dropIcon.setFont(Font.font("System", 18));
        dropZone.getChildren().addAll(dropIcon, dropText);

        HBox tglRow = new HBox(6, fHari, fBulan, fTahun);
        tglRow.setAlignment(Pos.CENTER_LEFT);

        GridPane grid = new GridPane();
        grid.setHgap(16); grid.setVgap(12);
        ColumnConstraints c0 = new ColumnConstraints(); c0.setHgrow(Priority.ALWAYS);
        ColumnConstraints c1 = new ColumnConstraints(160);
        grid.getColumnConstraints().addAll(c0, c1);

        grid.add(cardLabel("Judul Renungan"), 0, 0);
        grid.add(fJudul, 0, 1);
        grid.add(cardLabel("Tanggal Renungan"), 1, 0);
        grid.add(tglRow, 1, 1);
        grid.add(cardLabel("Isi Renungan"), 0, 2);
        grid.add(cardLabel("Tambahkan Foto *(opsional)"), 1, 2);
        grid.add(fIsi, 0, 3);
        grid.add(dropZone, 1, 3);

        Button btnTambah = primaryButton("Tambahkan  Renungan");
        HBox btnRow = new HBox(btnTambah);
        btnRow.setAlignment(Pos.CENTER);
        btnRow.setPadding(new Insets(4, 0, 0, 0));

        Label lblStatus = new Label();
        lblStatus.setTextFill(Color.WHITE);

        btnTambah.setOnAction(e -> {
            String judul = fJudul.getText().trim();
            String isi   = fIsi.getText().trim();
            if (judul.isEmpty() || isi.isEmpty()) {
                lblStatus.setText("⚠ Judul dan isi renungan wajib diisi.");
                lblStatus.setTextFill(Color.web("#FFB347"));
                return;
            }
            try {
                LocalDate tgl = LocalDate.of(
                        Integer.parseInt(fTahun.getText().trim()),
                        Integer.parseInt(fBulan.getText().trim()),
                        Integer.parseInt(fHari.getText().trim()));
                Renungan r = new Renungan();
                r.setJudul(judul); r.setIsi(isi); r.setTanggal(tgl);
                r.setIdUser(currentUser != null ? currentUser.getIdUser() : "");
                if (renunganDAO.insert(r)) {
                    lblStatus.setText("✅ Renungan berhasil disimpan!");
                    lblStatus.setTextFill(Color.web("#55EFC4"));
                    fJudul.clear(); fIsi.clear();
                    refreshListRenungan();
                }
            } catch (Exception ex) {
                lblStatus.setText("⚠ Periksa kembali isian tanggal.");
                lblStatus.setTextFill(Color.web("#FFB347"));
            }
        });

        VBox card = new VBox(12, grid, btnRow, lblStatus);
        card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color: " + CARD + "; -fx-background-radius: 10;");
        return card;
    }

    private void refreshListRenungan() {
        if (listRenunganBox == null) return;
        listRenunganBox.getChildren().clear();
        List<Renungan> list = renunganDAO.getAll();
        if (list.isEmpty()) {
            Label empty = new Label("Belum ada renungan.");
            empty.setTextFill(Color.web("#AAAAAA"));
            listRenunganBox.getChildren().add(empty);
            return;
        }
        for (Renungan item : list) {
            listRenunganBox.getChildren().add(buildRenunganRow(item));
        }
    }

    private HBox buildRenunganRow(Renungan item) {
        HBox row = new HBox(0);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 14, 10, 14));
        row.setStyle("-fx-background-color: #525252; -fx-background-radius: 8;");
        row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: #5a5a5a; -fx-background-radius: 8;"));
        row.setOnMouseExited(e -> row.setStyle("-fx-background-color: #525252; -fx-background-radius: 8;"));

        Label lJudul = rowLabel(item.getJudul(), true); lJudul.setPrefWidth(160);
        String isiShort = item.getIsi() != null && item.getIsi().length() > 70
                ? item.getIsi().substring(0, 70) + " ..." : item.getIsi();
        Label lIsi = rowLabel(isiShort != null ? isiShort : "-", false);
        HBox.setHgrow(lIsi, Priority.ALWAYS);
        Label lTgl = rowLabel(item.getTanggalStr(), false); lTgl.setPrefWidth(100);

        Button btnEdit  = actionBtn("✎ Edit",   ACCENT);
        Button btnHapus = actionBtn("🗑 Hapus", DANGER);

        btnEdit.setOnAction(e -> showEditRenunganDialog(item));
        btnHapus.setOnAction(e -> {
            if (confirmDelete()) { renunganDAO.delete(item.getIdRenungan()); refreshListRenungan(); }
        });

        row.getChildren().addAll(
                lJudul, divider(), lIsi, divider(), lTgl,
                new Region() {{ HBox.setHgrow(this, Priority.ALWAYS); }},
                btnEdit, spacer(6), btnHapus
        );
        return row;
    }

    private void showEditRenunganDialog(Renungan item) {
        Dialog<Renungan> dlg = new Dialog<>();
        dlg.setTitle("Edit Renungan");
        TextField fJudul = new TextField(item.getJudul());
        TextArea  fIsi   = new TextArea(item.getIsi()); fIsi.setPrefRowCount(6); fIsi.setWrapText(true);
        DatePicker fTgl  = new DatePicker(item.getTanggal());

        GridPane grid = dialogGrid();
        grid.add(dlgLabel("Judul"), 0, 0);        grid.add(fJudul, 1, 0);
        grid.add(dlgLabel("Isi Renungan"), 0, 1); grid.add(fIsi, 1, 1);
        grid.add(dlgLabel("Tanggal"), 0, 2);       grid.add(fTgl, 1, 2);

        dlg.getDialogPane().setContent(grid);
        dlg.getDialogPane().setPrefWidth(480);
        ButtonType ok = new ButtonType("Simpan", ButtonBar.ButtonData.OK_DONE);
        dlg.getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);
        dlg.setResultConverter(b -> {
            if (b == ok) {
                item.setJudul(fJudul.getText().trim());
                item.setIsi(fIsi.getText().trim());
                item.setTanggal(fTgl.getValue());
                return item;
            }
            return null;
        });
        dlg.showAndWait().ifPresent(r -> { renunganDAO.update(r); refreshListRenungan(); });
    }

    // ════════════════════════════════════════════════════════════════════
    //  HELPER: COPY FOTO KE UPLOADS
    // ════════════════════════════════════════════════════════════════════

    /**
     * Salin file foto ke folder uploads/pengumuman/.
     * Nama file dibuat unik pakai timestamp.
     * @return path tujuan jika berhasil, null jika gagal
     */
    private String copyFotoToUploads(String sourcePath, Label statusLabel) {
        try {
            File sourceFile = new File(sourcePath);
            if (!sourceFile.exists()) {
                statusLabel.setText("⚠ File foto tidak ditemukan.");
                statusLabel.setTextFill(Color.web("#FFB347"));
                return null;
            }

            // Validasi ukuran maks 5MB
            long fileSizeKB = sourceFile.length() / 1024;
            if (fileSizeKB > 5120) {
                statusLabel.setText("⚠ Ukuran foto terlalu besar (maks 5MB). Ukuran: " + fileSizeKB + " KB");
                statusLabel.setTextFill(Color.web("#FFB347"));
                return null;
            }

            // Buat folder tujuan
            Path uploadDir = Paths.get("uploads", "pengumuman");
            Files.createDirectories(uploadDir);

            // Nama file unik: timestamp + ekstensi asli
            String originalName = sourceFile.getName();
            String ext = "";
            int dotIdx = originalName.lastIndexOf('.');
            if (dotIdx > 0) ext = originalName.substring(dotIdx);

            String uniqueName = System.currentTimeMillis() + ext;
            Path destination  = uploadDir.resolve(uniqueName);

            Files.copy(sourceFile.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);
            return destination.toString();

        } catch (IOException ex) {
            statusLabel.setText("⚠ Gagal menyimpan foto: " + ex.getMessage());
            statusLabel.setTextFill(Color.web("#FFB347"));
            return null;
        }
    }

    // ════════════════════════════════════════════════════════════════════
    //  HELPERS UI
    // ════════════════════════════════════════════════════════════════════

    private boolean confirmDelete() {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, "Hapus data ini?", ButtonType.OK, ButtonType.CANCEL);
        a.setTitle("Konfirmasi Hapus"); a.setHeaderText(null);
        return a.showAndWait().filter(r -> r == ButtonType.OK).isPresent();
    }

    private Text sectionTitle(String text) {
        Text t = new Text(text);
        t.setFill(Color.WHITE);
        t.setFont(Font.font("System", FontWeight.BOLD, 16));
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
                "-fx-border-width: 0; -fx-padding: 10 12 10 12; -fx-font-size: 13;");
        f.setMaxWidth(Double.MAX_VALUE);
        return f;
    }

    private TextArea styledTextArea(String prompt) {
        TextArea ta = new TextArea();
        ta.setPromptText(prompt);
        ta.setPrefRowCount(4);
        ta.setWrapText(true);
        ta.setStyle("-fx-background-color: " + INPUT + "; -fx-text-fill: white; " +
                "-fx-prompt-text-fill: rgba(255,255,255,0.4); -fx-background-radius: 8; " +
                "-fx-border-width: 0; -fx-padding: 10 12 10 12; -fx-font-size: 13; " +
                "-fx-control-inner-background: " + INPUT + ";");
        return ta;
    }

    private TextField styledSmallField(String prompt) {
        TextField f = styledField(prompt);
        f.setPrefWidth(52); f.setMaxWidth(52);
        f.setAlignment(Pos.CENTER);
        return f;
    }

    private Button primaryButton(String text) {
        Button btn = new Button(text);
        btn.setFont(Font.font("System", FontWeight.BOLD, 13));
        btn.setPadding(new Insets(10, 36, 10, 36));
        String style = "-fx-background-color: " + ACCENT + "; -fx-text-fill: white; " +
                "-fx-background-radius: 8; -fx-cursor: hand; -fx-border-width: 0;";
        String hover = "-fx-background-color: #3D6FD4; -fx-text-fill: white; " +
                "-fx-background-radius: 8; -fx-cursor: hand; -fx-border-width: 0;";
        btn.setStyle(style);
        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited(e -> btn.setStyle(style));
        return btn;
    }

    private Button actionBtn(String text, String color) {
        Button btn = new Button(text);
        btn.setFont(Font.font("System", 12));
        btn.setPadding(new Insets(5, 12, 5, 12));
        btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; " +
                "-fx-background-radius: 6; -fx-cursor: hand; -fx-border-width: 0;");
        return btn;
    }

    private Region divider() {
        Region r = new Region();
        r.setPrefWidth(1); r.setPrefHeight(20);
        r.setStyle("-fx-background-color: " + DIVIDER + ";");
        HBox.setMargin(r, new Insets(0, 4, 0, 4));
        return r;
    }

    private Region spacer(double w) {
        Region r = new Region(); r.setPrefWidth(w); return r;
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
