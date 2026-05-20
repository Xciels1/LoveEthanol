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
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Panel manajemen data untuk Admin.
 * Fitur:
 *  - Kelola Jadwal Ibadah + informasi pendeta bertugas
 *  - Kelola Jadwal Tugas  + dropdown nama majelis
 *  - Kelola Akun          + tambah/edit/hapus admin & majelis
 */
public class AdminView extends VBox {

    private User currentUser;
    private final JadwalIbadahDAO jadwalIbadahDAO = new JadwalIbadahDAO();
    private final JadwalTugasDAO  jadwalTugasDAO  = new JadwalTugasDAO();
    private final LokasiDAO       lokasiDAO       = new LokasiDAO();
    private final UserDAO         userDAO         = new UserDAO();

    // Three panes switched by nav
    private VBox paneIbadah;
    private VBox paneTugas;
    private VBox paneAkun;
    private StackPane contentStack;

    // List containers
    private VBox listIbadahBox;
    private VBox listTugasBox;
    private VBox listAkunBox;

    // Colors (dark theme)
    private static final String BG      = "#3B3B3B";
    private static final String CARD    = "#484848";
    private static final String INPUT   = "#5C5C5C";
    private static final String ACCENT  = "#5B8DEF";
    private static final String DANGER  = "#E74C3C";
    private static final String SUCCESS = "#27AE60";
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
        paneTugas .setVisible(false); paneTugas.setManaged(false);
        paneAkun  .setVisible(false); paneAkun.setManaged(false);
    }

    public void showJadwalTugas() {
        paneTugas .setVisible(true);  paneTugas.setManaged(true);
        paneIbadah.setVisible(false); paneIbadah.setManaged(false);
        paneAkun  .setVisible(false); paneAkun.setManaged(false);
    }

    public void showKelolAkun() {
        paneAkun  .setVisible(true);  paneAkun.setManaged(true);
        paneIbadah.setVisible(false); paneIbadah.setManaged(false);
        paneTugas .setVisible(false); paneTugas.setManaged(false);
    }

    public void refresh() {
        refreshListIbadah();
        refreshListTugas();
        refreshListAkun();
    }

    private void buildUI() {
        setStyle("-fx-background-color: " + BG + ";");
        VBox.setVgrow(this, Priority.ALWAYS);

        contentStack = new StackPane();
        VBox.setVgrow(contentStack, Priority.ALWAYS);

        paneIbadah = buildJadwalIbadahPane();
        paneTugas  = buildJadwalTugasPane();
        paneAkun   = buildKelolAkunPane();

        // Default: paneIbadah visible
        paneTugas.setVisible(false); paneTugas.setManaged(false);
        paneAkun .setVisible(false); paneAkun.setManaged(false);

        contentStack.getChildren().addAll(paneIbadah, paneTugas, paneAkun);
        getChildren().add(contentStack);

        refreshListIbadah();
        refreshListTugas();
        refreshListAkun();
    }

    // ════════════════════════════════════════════════════════════════════
    //  PANE 1: JADWAL IBADAH  (+nama pendeta)
    // ════════════════════════════════════════════════════════════════════

    private VBox buildJadwalIbadahPane() {
        VBox pane = new VBox(0);
        pane.setStyle("-fx-background-color: " + BG + ";");
        VBox.setVgrow(pane, Priority.ALWAYS);

        HBox header = ChatView.buildPageHeader("Kelola Jadwal Ibadah");
        VBox.setMargin(header, new Insets(12, 12, 0, 12));

        ScrollPane scroll = buildScrollPane();
        VBox content = buildScrollContent();

        content.getChildren().add(sectionTitle("Tambah Jadwal Ibadah"));
        content.getChildren().add(buildIbadahFormCard());
        content.getChildren().add(sectionTitle("Daftar Ibadah"));

        listIbadahBox = new VBox(8);
        listIbadahBox.setStyle("-fx-background-color:" + CARD + "; -fx-background-radius:10; -fx-padding:8;");
        content.getChildren().add(listIbadahBox);

        scroll.setContent(content);
        pane.getChildren().addAll(header, scroll);
        return pane;
    }

    private VBox buildIbadahFormCard() {
        TextField fNama    = styledField("Nama Ibadah");
        TextField fLokasi  = styledField("Lokasi Ibadah");
        TextField fPendeta = styledField("Nama Pendeta yang Bertugas");  // ← BARU

        TextField fHari  = styledSmallField("DD");
        TextField fBulan = styledSmallField("MM");
        TextField fTahun = styledSmallField("YYYY"); fTahun.setPrefWidth(60);
        TextField fJam   = styledSmallField("00");
        TextField fMenit = styledSmallField("00");

        LocalDate today = LocalDate.now();
        fHari .setText(String.format("%02d", today.getDayOfMonth()));
        fBulan.setText(String.format("%02d", today.getMonthValue()));
        fTahun.setText(String.valueOf(today.getYear()));
        fJam.setText("07"); fMenit.setText("00");

        HBox tglRow = new HBox(6, fHari, fBulan, fTahun);
        tglRow.setAlignment(Pos.CENTER_LEFT);

        HBox wktRow = new HBox(6, fJam,
                colon(), fMenit,
                wibLabel());
        wktRow.setAlignment(Pos.CENTER_LEFT);

        GridPane grid = new GridPane();
        grid.setHgap(16); grid.setVgap(12);
        ColumnConstraints c0 = new ColumnConstraints(); c0.setHgrow(Priority.ALWAYS);
        ColumnConstraints c1 = new ColumnConstraints(180);
        grid.getColumnConstraints().addAll(c0, c1);

        grid.add(cardLabel("Nama Ibadah"),     0, 0); grid.add(fNama,    0, 1);
        grid.add(cardLabel("Tanggal Ibadah"),  1, 0); grid.add(tglRow,   1, 1);
        grid.add(cardLabel("Lokasi Ibadah"),   0, 2); grid.add(fLokasi,  0, 3);
        grid.add(cardLabel("Waktu Ibadah"),    1, 2); grid.add(wktRow,   1, 3);
        // Pendeta — span 2 kolom
        grid.add(cardLabel("Nama Pendeta yang Bertugas"), 0, 4);
        GridPane.setColumnSpan(cardLabel("Nama Pendeta yang Bertugas"), 2);
        grid.add(fPendeta, 0, 5);
        GridPane.setColumnSpan(fPendeta, 2);

        Button btnTambah = primaryButton("Tambahkan  Jadwal");
        HBox btnRow = new HBox(btnTambah);
        btnRow.setAlignment(Pos.CENTER);
        btnRow.setPadding(new Insets(6, 0, 0, 0));

        Label lblStatus = new Label();
        lblStatus.setTextFill(Color.WHITE);

        btnTambah.setOnAction(e -> {
            try {
                String nama = fNama.getText().trim();
                if (nama.isEmpty()) {
                    lblStatus.setText("⚠ Nama ibadah wajib diisi.");
                    lblStatus.setTextFill(Color.web("#FFB347")); return;
                }
                LocalDate tgl = LocalDate.of(
                        Integer.parseInt(fTahun.getText().trim()),
                        Integer.parseInt(fBulan.getText().trim()),
                        Integer.parseInt(fHari.getText().trim()));
                LocalTime wkt = LocalTime.of(
                        Integer.parseInt(fJam.getText().trim()),
                        Integer.parseInt(fMenit.getText().trim()));

                String idLokasi = findOrCreateLokasi(fLokasi.getText().trim());

                JadwalIbadah j = new JadwalIbadah();
                j.setNamaIbadah(nama);
                j.setTanggal(tgl);
                j.setWaktu(wkt);
                j.setIdLokasi(idLokasi);
                j.setIdUser(currentUser != null ? currentUser.getIdUser() : "");
                j.setNamaPendeta(fPendeta.getText().trim());   // ← BARU

                if (jadwalIbadahDAO.insert(j)) {
                    lblStatus.setText("✅ Jadwal berhasil disimpan!");
                    lblStatus.setTextFill(Color.web("#55EFC4"));
                    fNama.clear(); fLokasi.clear(); fPendeta.clear();
                    refreshListIbadah();
                }
            } catch (Exception ex) {
                lblStatus.setText("⚠ Periksa isian tanggal dan waktu.");
                lblStatus.setTextFill(Color.web("#FFB347"));
            }
        });

        VBox card = new VBox(12, grid, btnRow, lblStatus);
        card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color:" + CARD + "; -fx-background-radius:10;");
        return card;
    }

    private void refreshListIbadah() {
        if (listIbadahBox == null) return;
        listIbadahBox.getChildren().clear();
        List<JadwalIbadah> list = jadwalIbadahDAO.getAll();
        if (list.isEmpty()) {
            listIbadahBox.getChildren().add(emptyLabel("Belum ada data jadwal ibadah."));
            return;
        }
        for (JadwalIbadah item : list) listIbadahBox.getChildren().add(buildIbadahRow(item));
    }

    private HBox buildIbadahRow(JadwalIbadah item) {
        HBox row = baseRow();

        Label lNama    = rowLabel(item.getNamaIbadah(), true);
        HBox.setHgrow(lNama, Priority.ALWAYS);

        Label lLokasi  = rowLabel(item.getNamaLokasi() != null ? item.getNamaLokasi() : "-", false);
        lLokasi.setPrefWidth(130);

        // Pendeta — tampilkan di baris
        String pendeta = (item.getNamaPendeta() != null && !item.getNamaPendeta().isBlank())
                ? item.getNamaPendeta() : "-";
        Label lPendeta = rowLabel(pendeta, false);
        lPendeta.setPrefWidth(130);

        Label lWkt = rowLabel(item.getWaktuStr(), false);   lWkt.setPrefWidth(80);
        Label lTgl = rowLabel(item.getTanggalStr(), false); lTgl.setPrefWidth(90);

        Button btnEdit  = actionBtn("✎ Edit",   ACCENT);
        Button btnHapus = actionBtn("🗑 Hapus", DANGER);

        btnEdit.setOnAction(e -> showEditIbadahDialog(item));
        btnHapus.setOnAction(e -> {
            if (confirmDelete()) { jadwalIbadahDAO.delete(item.getIdJadwal()); refreshListIbadah(); }
        });

        row.getChildren().addAll(
                lNama, divider(), lLokasi, divider(), lPendeta,
                divider(), lWkt, divider(), lTgl,
                new Region() {{ HBox.setHgrow(this, Priority.ALWAYS); }},
                btnEdit, spacer(6), btnHapus);
        return row;
    }

    private void showEditIbadahDialog(JadwalIbadah item) {
        Dialog<JadwalIbadah> dlg = new Dialog<>();
        dlg.setTitle("Edit Jadwal Ibadah");

        TextField fNama    = new TextField(item.getNamaIbadah());
        DatePicker fTgl    = new DatePicker(item.getTanggal());
        TextField fWaktu   = new TextField(item.getWaktu() != null
                ? item.getWaktu().toString().substring(0, 5) : "");
        TextField fPendeta = new TextField(item.getNamaPendeta() != null
                ? item.getNamaPendeta() : "");   // ← BARU
        fPendeta.setPromptText("Nama pendeta yang bertugas");

        ComboBox<String> fLokasi = new ComboBox<>();
        refreshLokasiCombo(fLokasi);
        if (item.getNamaLokasi() != null)
            fLokasi.setValue(item.getIdLokasi() + " | " + item.getNamaLokasi());

        GridPane grid = dialogGrid();
        grid.add(dlgLabel("Nama Ibadah"),   0, 0); grid.add(fNama,    1, 0);
        grid.add(dlgLabel("Tanggal"),       0, 1); grid.add(fTgl,     1, 1);
        grid.add(dlgLabel("Waktu (HH:MM)"), 0, 2); grid.add(fWaktu,   1, 2);
        grid.add(dlgLabel("Lokasi"),        0, 3); grid.add(fLokasi,  1, 3);
        grid.add(dlgLabel("Nama Pendeta"),  0, 4); grid.add(fPendeta, 1, 4);  // ← BARU

        dlg.getDialogPane().setContent(grid);
        dlg.getDialogPane().setPrefWidth(440);
        ButtonType ok = new ButtonType("Simpan", ButtonBar.ButtonData.OK_DONE);
        dlg.getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);

        dlg.setResultConverter(b -> {
            if (b == ok) {
                item.setNamaIbadah(fNama.getText().trim());
                item.setTanggal(fTgl.getValue());
                try { item.setWaktu(LocalTime.parse(fWaktu.getText().trim())); }
                catch (Exception ignored) {}
                if (fLokasi.getValue() != null)
                    item.setIdLokasi(fLokasi.getValue().split("\\|")[0].trim());
                item.setNamaPendeta(fPendeta.getText().trim());   // ← BARU
                return item;
            }
            return null;
        });
        dlg.showAndWait().ifPresent(j -> { jadwalIbadahDAO.update(j); refreshListIbadah(); });
    }

    // ════════════════════════════════════════════════════════════════════
    //  PANE 2: JADWAL TUGAS  (dropdown nama majelis)
    // ════════════════════════════════════════════════════════════════════

    private VBox buildJadwalTugasPane() {
        VBox pane = new VBox(0);
        pane.setStyle("-fx-background-color: " + BG + ";");
        VBox.setVgrow(pane, Priority.ALWAYS);

        HBox header = ChatView.buildPageHeader("Kelola Jadwal Tugas");
        VBox.setMargin(header, new Insets(12, 12, 0, 12));

        ScrollPane scroll = buildScrollPane();
        VBox content = buildScrollContent();

        content.getChildren().add(sectionTitle("Tambah Tugas"));
        content.getChildren().add(buildTugasFormCard());
        content.getChildren().add(sectionTitle("Daftar Tugas"));

        listTugasBox = new VBox(8);
        listTugasBox.setStyle("-fx-background-color:" + CARD + "; -fx-background-radius:10; -fx-padding:8;");
        content.getChildren().add(listTugasBox);

        scroll.setContent(content);
        pane.getChildren().addAll(header, scroll);
        return pane;
    }

    private VBox buildTugasFormCard() {
        // ── Dropdown nama majelis (bukan input ID manual) ──────────────
        // Map: nama → id_user
        LinkedHashMap<String, String> majelisMap = new LinkedHashMap<>();
        ComboBox<String> fMajelis = new ComboBox<>();
        fMajelis.setPromptText("Pilih Majelis...");
        fMajelis.setMaxWidth(Double.MAX_VALUE);
        fMajelis.setStyle("-fx-background-color:" + INPUT + "; -fx-text-fill:white; " +
                "-fx-background-radius:8; -fx-border-width:0;");
        userDAO.getAllMajelis().forEach(u -> {
            majelisMap.put(u.getNama(), u.getIdUser());
            fMajelis.getItems().add(u.getNama());
        });

        TextField fTugas = styledField("Deskripsi tugas");

        TextField fHari  = styledSmallField("DD");
        TextField fBulan = styledSmallField("MM");
        TextField fTahun = styledSmallField("YYYY"); fTahun.setPrefWidth(60);
        TextField fJam   = styledSmallField("00");
        TextField fMenit = styledSmallField("00");

        LocalDate today = LocalDate.now();
        fHari .setText(String.format("%02d", today.getDayOfMonth()));
        fBulan.setText(String.format("%02d", today.getMonthValue()));
        fTahun.setText(String.valueOf(today.getYear()));
        fJam.setText("09"); fMenit.setText("00");

        HBox tglRow = new HBox(6, fHari, fBulan, fTahun);
        tglRow.setAlignment(Pos.CENTER_LEFT);
        HBox wktRow = new HBox(6, fJam, colon(), fMenit, wibLabel());
        wktRow.setAlignment(Pos.CENTER_LEFT);

        GridPane grid = new GridPane();
        grid.setHgap(16); grid.setVgap(12);
        ColumnConstraints c0 = new ColumnConstraints(); c0.setHgrow(Priority.ALWAYS);
        ColumnConstraints c1 = new ColumnConstraints(200);
        grid.getColumnConstraints().addAll(c0, c1);

        grid.add(cardLabel("Majelis Bertugas"), 0, 0);
        grid.add(fMajelis, 0, 1);
        grid.add(cardLabel("Tanggal Tugas"), 1, 0);
        grid.add(tglRow, 1, 1);
        grid.add(cardLabel("Tugas"), 0, 2);
        grid.add(cardLabel("Waktu Ibadah"), 1, 2);
        grid.add(fTugas, 0, 3);
        grid.add(wktRow, 1, 3);

        Button btnTambah = primaryButton("Tambahkan  Tugas");
        HBox btnRow = new HBox(btnTambah);
        btnRow.setAlignment(Pos.CENTER);

        Label lblStatus = new Label();
        lblStatus.setTextFill(Color.WHITE);

        btnTambah.setOnAction(e -> {
            try {
                // Validasi majelis dipilih
                String selectedNama = fMajelis.getValue();
                if (selectedNama == null || selectedNama.isBlank()) {
                    lblStatus.setText("⚠ Pilih majelis terlebih dahulu.");
                    lblStatus.setTextFill(Color.web("#FFB347")); return;
                }
                String idUser = majelisMap.get(selectedNama);
                if (idUser == null) {
                    lblStatus.setText("⚠ Majelis tidak ditemukan di database.");
                    lblStatus.setTextFill(Color.web("#FFB347")); return;
                }

                String tugas = fTugas.getText().trim();
                if (tugas.isEmpty()) {
                    lblStatus.setText("⚠ Deskripsi tugas wajib diisi.");
                    lblStatus.setTextFill(Color.web("#FFB347")); return;
                }

                LocalDate tgl = LocalDate.of(
                        Integer.parseInt(fTahun.getText().trim()),
                        Integer.parseInt(fBulan.getText().trim()),
                        Integer.parseInt(fHari.getText().trim()));
                LocalTime wkt = LocalTime.of(
                        Integer.parseInt(fJam.getText().trim()),
                        Integer.parseInt(fMenit.getText().trim()));

                JadwalTugas jt = new JadwalTugas();
                jt.setIdUser(idUser);
                jt.setTugas(tugas);
                jt.setTanggal(tgl);
                jt.setWaktuIbadah(wkt);

                if (jadwalTugasDAO.insert(jt)) {
                    lblStatus.setText("✅ Tugas berhasil disimpan!");
                    lblStatus.setTextFill(Color.web("#55EFC4"));
                    fMajelis.setValue(null); fTugas.clear();
                    refreshListTugas();
                }
            } catch (Exception ex) {
                lblStatus.setText("⚠ Periksa isian tanggal dan waktu.");
                lblStatus.setTextFill(Color.web("#FFB347"));
            }
        });

        VBox card = new VBox(12, grid, btnRow, lblStatus);
        card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color:" + CARD + "; -fx-background-radius:10;");
        return card;
    }

    private void refreshListTugas() {
        if (listTugasBox == null) return;
        listTugasBox.getChildren().clear();
        List<JadwalTugas> list = jadwalTugasDAO.getAll();
        if (list.isEmpty()) {
            listTugasBox.getChildren().add(emptyLabel("Belum ada data jadwal tugas."));
            return;
        }
        for (JadwalTugas item : list) listTugasBox.getChildren().add(buildTugasRow(item));
    }

    private HBox buildTugasRow(JadwalTugas item) {
        HBox row = baseRow();

        String nama = item.getNamaMajelis() != null ? item.getNamaMajelis() : item.getIdUser();
        Label lNama  = rowLabel(nama, true);            lNama.setPrefWidth(120);
        Label lTugas = rowLabel(item.getTugas(), false);
        HBox.setHgrow(lTugas, Priority.ALWAYS);
        Label lWkt = rowLabel(item.getWaktuStr(), false);   lWkt.setPrefWidth(80);
        Label lTgl = rowLabel(item.getTanggalStr(), false); lTgl.setPrefWidth(90);

        Button btnEdit  = actionBtn("✎ Edit",   ACCENT);
        Button btnHapus = actionBtn("🗑 Hapus", DANGER);

        btnEdit.setOnAction(e -> showEditTugasDialog(item));
        btnHapus.setOnAction(e -> {
            if (confirmDelete()) { jadwalTugasDAO.delete(item.getIdTugas()); refreshListTugas(); }
        });

        row.getChildren().addAll(lNama, divider(), lTugas, divider(), lWkt, divider(), lTgl,
                new Region() {{ HBox.setHgrow(this, Priority.ALWAYS); }},
                btnEdit, spacer(6), btnHapus);
        return row;
    }

    private void showEditTugasDialog(JadwalTugas item) {
        Dialog<JadwalTugas> dlg = new Dialog<>();
        dlg.setTitle("Edit Jadwal Tugas");

        // Dropdown nama majelis
        LinkedHashMap<String, String> majelisMap = new LinkedHashMap<>();
        ComboBox<String> fMajelis = new ComboBox<>();
        fMajelis.setMaxWidth(Double.MAX_VALUE);
        userDAO.getAllMajelis().forEach(u -> {
            majelisMap.put(u.getNama(), u.getIdUser());
            fMajelis.getItems().add(u.getNama());
        });
        // Set nilai saat ini
        if (item.getNamaMajelis() != null) {
            fMajelis.setValue(item.getNamaMajelis());
        } else {
            // Cari nama dari id_user
            majelisMap.forEach((nama, id) -> {
                if (id.equals(item.getIdUser())) fMajelis.setValue(nama);
            });
        }

        TextField  fTugas = new TextField(item.getTugas());
        DatePicker fTgl   = new DatePicker(item.getTanggal());
        TextField  fWaktu = new TextField(
                item.getWaktuStr().replace(" WIB", "").equals("-")
                        ? "" : item.getWaktuStr().replace(" WIB", ""));

        GridPane grid = dialogGrid();
        grid.add(dlgLabel("Majelis"),       0, 0); grid.add(fMajelis, 1, 0);
        grid.add(dlgLabel("Tugas"),         0, 1); grid.add(fTugas,   1, 1);
        grid.add(dlgLabel("Tanggal"),       0, 2); grid.add(fTgl,     1, 2);
        grid.add(dlgLabel("Waktu (HH:MM)"),0, 3); grid.add(fWaktu,   1, 3);

        dlg.getDialogPane().setContent(grid);
        dlg.getDialogPane().setPrefWidth(420);
        ButtonType ok = new ButtonType("Simpan", ButtonBar.ButtonData.OK_DONE);
        dlg.getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);

        dlg.setResultConverter(b -> {
            if (b == ok) {
                item.setTugas(fTugas.getText().trim());
                item.setTanggal(fTgl.getValue());
                String selNama = fMajelis.getValue();
                if (selNama != null) {
                    String idUser = majelisMap.get(selNama);
                    if (idUser != null) item.setIdUser(idUser);
                }
                try { item.setWaktuIbadah(LocalTime.parse(fWaktu.getText().trim())); }
                catch (Exception ignored) {}
                return item;
            }
            return null;
        });
        dlg.showAndWait().ifPresent(jt -> { jadwalTugasDAO.update(jt); refreshListTugas(); });
    }

    // ════════════════════════════════════════════════════════════════════
    //  PANE 3: KELOLA AKUN (Tambah / Edit / Hapus admin & majelis)
    // ════════════════════════════════════════════════════════════════════

    private VBox buildKelolAkunPane() {
        VBox pane = new VBox(0);
        pane.setStyle("-fx-background-color: " + BG + ";");
        VBox.setVgrow(pane, Priority.ALWAYS);

        HBox header = ChatView.buildPageHeader("Kelola Akun");
        VBox.setMargin(header, new Insets(12, 12, 0, 12));

        ScrollPane scroll = buildScrollPane();
        VBox content = buildScrollContent();

        content.getChildren().add(sectionTitle("Tambah Akun Baru"));
        content.getChildren().add(buildAkunFormCard());
        content.getChildren().add(sectionTitle("Daftar Akun Admin & Majelis"));

        listAkunBox = new VBox(8);
        listAkunBox.setStyle("-fx-background-color:" + CARD + "; -fx-background-radius:10; -fx-padding:8;");
        content.getChildren().add(listAkunBox);

        scroll.setContent(content);
        pane.getChildren().addAll(header, scroll);
        return pane;
    }

    private VBox buildAkunFormCard() {
        TextField     fNama     = styledField("Nama lengkap");
        TextField     fUsername = styledField("Username");
        PasswordField fPassword = styledPasswordField("Password (min. 6 karakter)");
        PasswordField fConfirm  = styledPasswordField("Konfirmasi password");

        ComboBox<String> fRole = new ComboBox<>();
        fRole.getItems().addAll("Majelis", "Admin");
        fRole.setValue("Majelis");
        fRole.setMaxWidth(Double.MAX_VALUE);
        fRole.setStyle("-fx-background-color:" + INPUT + "; -fx-background-radius:8; " +
                "-fx-border-width:0; -fx-text-fill:white;");

        GridPane grid = new GridPane();
        grid.setHgap(16); grid.setVgap(12);
        ColumnConstraints c0 = new ColumnConstraints(); c0.setHgrow(Priority.ALWAYS);
        ColumnConstraints c1 = new ColumnConstraints(); c1.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(c0, c1);

        grid.add(cardLabel("Nama Lengkap"), 0, 0);      grid.add(fNama,     0, 1);
        grid.add(cardLabel("Role"),         1, 0);      grid.add(fRole,     1, 1);
        grid.add(cardLabel("Username"),     0, 2);      grid.add(fUsername, 0, 3);
        grid.add(cardLabel("Password"),     1, 2);      grid.add(fPassword, 1, 3);
        grid.add(cardLabel("Konfirmasi Password"), 0, 4);
        GridPane.setColumnSpan(cardLabel("Konfirmasi Password"), 2);
        grid.add(fConfirm, 0, 5);
        GridPane.setColumnSpan(fConfirm, 2);

        Button btnTambah = primaryButton("Tambahkan  Akun");
        HBox btnRow = new HBox(btnTambah);
        btnRow.setAlignment(Pos.CENTER);

        Label lblStatus = new Label();
        lblStatus.setTextFill(Color.WHITE);

        btnTambah.setOnAction(e -> {
            String nama     = fNama.getText().trim();
            String username = fUsername.getText().trim();
            String password = fPassword.getText().trim();
            String confirm  = fConfirm.getText().trim();
            String role     = fRole.getValue().toLowerCase();

            if (nama.isEmpty() || username.isEmpty() || password.isEmpty()) {
                lblStatus.setText("⚠ Nama, username, dan password wajib diisi.");
                lblStatus.setTextFill(Color.web("#FFB347")); return;
            }
            if (!password.equals(confirm)) {
                lblStatus.setText("⚠ Password dan konfirmasi tidak cocok.");
                lblStatus.setTextFill(Color.web("#FFB347")); return;
            }
            if (password.length() < 6) {
                lblStatus.setText("⚠ Password minimal 6 karakter.");
                lblStatus.setTextFill(Color.web("#FFB347")); return;
            }

            User newUser = "admin".equals(role) ? new Admin() : new Majelis();
            newUser.setNama(nama);
            newUser.setUsername(username);
            newUser.setPassword(password);
            newUser.setRole(role);

            if (userDAO.insert(newUser)) {
                lblStatus.setText("✅ Akun " + fRole.getValue() + " berhasil ditambahkan!");
                lblStatus.setTextFill(Color.web("#55EFC4"));
                fNama.clear(); fUsername.clear();
                fPassword.clear(); fConfirm.clear();
                fRole.setValue("Majelis");
                refreshListAkun();
            } else {
                lblStatus.setText("⚠ Gagal menyimpan. Username mungkin sudah digunakan.");
                lblStatus.setTextFill(Color.web("#FFB347"));
            }
        });

        VBox card = new VBox(12, grid, btnRow, lblStatus);
        card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color:" + CARD + "; -fx-background-radius:10;");
        return card;
    }

    private void refreshListAkun() {
        if (listAkunBox == null) return;
        listAkunBox.getChildren().clear();

        List<User> admins  = userDAO.getUsersByRole("admin");
        List<User> majelis = userDAO.getUsersByRole("majelis");

        if (admins.isEmpty() && majelis.isEmpty()) {
            listAkunBox.getChildren().add(emptyLabel("Belum ada akun terdaftar."));
            return;
        }

        // Divider label
        if (!admins.isEmpty()) {
            Label lbAdmin = new Label("  Admin");
            lbAdmin.setTextFill(Color.web("#AAAAAA"));
            lbAdmin.setFont(Font.font("System", FontWeight.BOLD, 11));
            listAkunBox.getChildren().add(lbAdmin);
            admins.forEach(u -> listAkunBox.getChildren().add(buildAkunRow(u)));
        }
        if (!majelis.isEmpty()) {
            Label lbMaj = new Label("  Majelis");
            lbMaj.setTextFill(Color.web("#AAAAAA"));
            lbMaj.setFont(Font.font("System", FontWeight.BOLD, 11));
            lbMaj.setPadding(new Insets(8, 0, 0, 0));
            listAkunBox.getChildren().add(lbMaj);
            majelis.forEach(u -> listAkunBox.getChildren().add(buildAkunRow(u)));
        }
    }

    private HBox buildAkunRow(User user) {
        HBox row = baseRow();

        // Highlight akun sendiri
        if (currentUser != null && user.getIdUser().equals(currentUser.getIdUser())) {
            row.setStyle("-fx-background-color: #4A5E7A; -fx-background-radius:8;");
        }

        Label lNama     = rowLabel(user.getNama(), true);    lNama.setPrefWidth(160);
        Label lUsername = rowLabel(user.getUsername(), false); lUsername.setPrefWidth(130);
        String roleStr  = user.getRole().substring(0, 1).toUpperCase() + user.getRole().substring(1);
        Label lRole     = rowLabel(roleStr, false);           lRole.setPrefWidth(80);

        Button btnEdit  = actionBtn("✎ Edit",   ACCENT);
        Button btnHapus = actionBtn("🗑 Hapus", DANGER);

        // Nonaktifkan hapus untuk akun sendiri
        boolean isSelf = currentUser != null && user.getIdUser().equals(currentUser.getIdUser());
        btnHapus.setDisable(isSelf);
        if (isSelf) {
            btnHapus.setStyle("-fx-background-color:#666666; -fx-text-fill:white; " +
                    "-fx-background-radius:6; -fx-cursor:default;");
        }

        btnEdit.setOnAction(e -> showEditAkunDialog(user));
        btnHapus.setOnAction(e -> {
            if (isSelf) return;
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Konfirmasi Hapus");
            confirm.setHeaderText(null);
            confirm.setContentText("Hapus akun \"" + user.getNama() + "\" (" + roleStr + ")?");
            confirm.showAndWait().filter(r -> r == ButtonType.OK).ifPresent(r -> {
                userDAO.delete(user.getIdUser());
                refreshListAkun();
            });
        });

        Label selfBadge = new Label(isSelf ? " (Anda)" : "");
        selfBadge.setTextFill(Color.web("#7FB3D3"));
        selfBadge.setFont(Font.font("System", 11));

        row.getChildren().addAll(lNama, selfBadge, divider(), lUsername, divider(), lRole,
                new Region() {{ HBox.setHgrow(this, Priority.ALWAYS); }},
                btnEdit, spacer(6), btnHapus);
        return row;
    }

    private void showEditAkunDialog(User item) {
        Dialog<User> dlg = new Dialog<>();
        dlg.setTitle("Edit Akun — " + item.getNama());

        TextField     fNama     = new TextField(item.getNama());
        TextField     fUsername = new TextField(item.getUsername());
        PasswordField fPassword = new PasswordField();
        fPassword.setPromptText("Kosongkan jika tidak ingin ganti password");
        PasswordField fConfirm  = new PasswordField();
        fConfirm.setPromptText("Konfirmasi password baru");

        ComboBox<String> fRole = new ComboBox<>();
        fRole.getItems().addAll("Majelis", "Admin");
        fRole.setValue(item.getRole().substring(0, 1).toUpperCase() + item.getRole().substring(1));
        // Cegah ganti role akun sendiri
        boolean isSelf = currentUser != null && item.getIdUser().equals(currentUser.getIdUser());
        fRole.setDisable(isSelf);

        GridPane grid = dialogGrid();
        grid.add(dlgLabel("Nama Lengkap"), 0, 0); grid.add(fNama,     1, 0);
        grid.add(dlgLabel("Username"),     0, 1); grid.add(fUsername, 1, 1);
        grid.add(dlgLabel("Role"),         0, 2); grid.add(fRole,     1, 2);
        grid.add(dlgLabel("Password Baru"),0, 3); grid.add(fPassword, 1, 3);
        grid.add(dlgLabel("Konfirmasi"),   0, 4); grid.add(fConfirm,  1, 4);

        if (isSelf) {
            Label info = new Label("ℹ️ Role tidak bisa diubah untuk akun sendiri.");
            info.setFont(Font.font("System", 11));
            info.setTextFill(Color.web("#718096"));
            grid.add(info, 0, 5);
            GridPane.setColumnSpan(info, 2);
        }

        dlg.getDialogPane().setContent(grid);
        dlg.getDialogPane().setPrefWidth(430);
        ButtonType ok = new ButtonType("Simpan", ButtonBar.ButtonData.OK_DONE);
        dlg.getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);

        dlg.setResultConverter(b -> {
            if (b == ok) {
                if (fNama.getText().trim().isEmpty() || fUsername.getText().trim().isEmpty()) {
                    ChatAjaApp.showAlert(Alert.AlertType.ERROR, "Error", "Nama dan username wajib diisi.");
                    return null;
                }
                if (!fPassword.getText().isEmpty()) {
                    if (!fPassword.getText().equals(fConfirm.getText())) {
                        ChatAjaApp.showAlert(Alert.AlertType.ERROR, "Error",
                                "Password dan konfirmasi tidak cocok.");
                        return null;
                    }
                    if (fPassword.getText().length() < 6) {
                        ChatAjaApp.showAlert(Alert.AlertType.ERROR, "Error",
                                "Password minimal 6 karakter.");
                        return null;
                    }
                    item.setPassword(fPassword.getText().trim());
                }
                item.setNama(fNama.getText().trim());
                item.setUsername(fUsername.getText().trim());
                if (!isSelf) item.setRole(fRole.getValue().toLowerCase());
                return item;
            }
            return null;
        });
        dlg.showAndWait().ifPresent(u -> { userDAO.update(u); refreshListAkun(); });
    }

    // ════════════════════════════════════════════════════════════════════
    //  HELPERS
    // ════════════════════════════════════════════════════════════════════

    private String findOrCreateLokasi(String namaLokasi) {
        if (namaLokasi == null || namaLokasi.isEmpty()) return "";
        for (Lokasi l : lokasiDAO.getAll()) {
            if (l.getNamaTempat().equalsIgnoreCase(namaLokasi)) return l.getIdLokasi();
        }
        Lokasi newLok = new Lokasi();
        newLok.setNamaTempat(namaLokasi);
        newLok.setAlamat(namaLokasi);
        newLok.setKontak("");
        newLok.setIdUser(currentUser != null ? currentUser.getIdUser() : "");
        lokasiDAO.insert(newLok);
        for (Lokasi l : lokasiDAO.getAll()) {
            if (l.getNamaTempat().equalsIgnoreCase(namaLokasi)) return l.getIdLokasi();
        }
        return "";
    }

    private void refreshLokasiCombo(ComboBox<String> combo) {
        combo.getItems().clear();
        lokasiDAO.getAll().forEach(l ->
                combo.getItems().add(l.getIdLokasi() + " | " + l.getNamaTempat()));
    }

    private boolean confirmDelete() {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, "Hapus data ini?",
                ButtonType.OK, ButtonType.CANCEL);
        a.setTitle("Konfirmasi Hapus"); a.setHeaderText(null);
        return a.showAndWait().filter(r -> r == ButtonType.OK).isPresent();
    }

    // ── Layout helpers ────────────────────────────────────────────────────

    private ScrollPane buildScrollPane() {
        ScrollPane sp = new ScrollPane();
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background-color:" + BG + "; -fx-background:" + BG + "; -fx-border-width:0;");
        VBox.setVgrow(sp, Priority.ALWAYS);
        return sp;
    }

    private VBox buildScrollContent() {
        VBox v = new VBox(20);
        v.setPadding(new Insets(16, 16, 24, 16));
        v.setStyle("-fx-background-color:" + BG + ";");
        return v;
    }

    private HBox baseRow() {
        HBox row = new HBox(0);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 14, 10, 14));
        row.setStyle("-fx-background-color:#525252; -fx-background-radius:8;");
        row.setOnMouseEntered(e -> row.setStyle("-fx-background-color:#5a5a5a; -fx-background-radius:8;"));
        row.setOnMouseExited(e -> row.setStyle("-fx-background-color:#525252; -fx-background-radius:8;"));
        return row;
    }

    private Label emptyLabel(String text) {
        Label l = new Label(text);
        l.setTextFill(Color.web("#AAAAAA"));
        return l;
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
        f.setStyle("-fx-background-color:" + INPUT + "; -fx-text-fill:white; " +
                "-fx-prompt-text-fill:rgba(255,255,255,0.4); -fx-background-radius:8; " +
                "-fx-border-width:0; -fx-padding:10 12 10 12; -fx-font-size:13;");
        f.setMaxWidth(Double.MAX_VALUE);
        return f;
    }

    private PasswordField styledPasswordField(String prompt) {
        PasswordField f = new PasswordField();
        f.setPromptText(prompt);
        f.setStyle("-fx-background-color:" + INPUT + "; -fx-text-fill:white; " +
                "-fx-prompt-text-fill:rgba(255,255,255,0.4); -fx-background-radius:8; " +
                "-fx-border-width:0; -fx-padding:10 12 10 12; -fx-font-size:13;");
        f.setMaxWidth(Double.MAX_VALUE);
        return f;
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
        String style = "-fx-background-color:" + ACCENT + "; -fx-text-fill:white; " +
                "-fx-background-radius:8; -fx-cursor:hand; -fx-border-width:0;";
        String hover = "-fx-background-color:#3D6FD4; -fx-text-fill:white; " +
                "-fx-background-radius:8; -fx-cursor:hand; -fx-border-width:0;";
        btn.setStyle(style);
        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited(e -> btn.setStyle(style));
        return btn;
    }

    private Button actionBtn(String text, String color) {
        Button btn = new Button(text);
        btn.setFont(Font.font("System", 12));
        btn.setPadding(new Insets(5, 12, 5, 12));
        btn.setStyle("-fx-background-color:" + color + "; -fx-text-fill:white; " +
                "-fx-background-radius:6; -fx-cursor:hand; -fx-border-width:0;");
        return btn;
    }

    private Region divider() {
        Region r = new Region();
        r.setPrefWidth(1); r.setPrefHeight(20);
        r.setStyle("-fx-background-color:" + DIVIDER + ";");
        HBox.setMargin(r, new Insets(0, 4, 0, 4));
        return r;
    }

    private Region spacer(double w) { Region r = new Region(); r.setPrefWidth(w); return r; }

    private Label colon() {
        Label l = new Label(":");
        l.setTextFill(Color.WHITE);
        l.setFont(Font.font("System", FontWeight.BOLD, 14));
        return l;
    }

    private Label wibLabel() {
        Label l = new Label("WIB");
        l.setTextFill(Color.WHITE);
        return l;
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
