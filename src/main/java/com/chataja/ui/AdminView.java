package com.chataja.ui;

import com.chataja.dao.*;
import com.chataja.model.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
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
 * UC-3: Kelola Jadwal Ibadah (CRUD)
 * UC-3: Kelola Jadwal Tugas (CRUD)
 */
public class AdminView extends VBox {

    private User currentUser;
    private final JadwalIbadahDAO jadwalIbadahDAO = new JadwalIbadahDAO();
    private final JadwalTugasDAO jadwalTugasDAO = new JadwalTugasDAO();
    private final LokasiDAO lokasiDAO = new LokasiDAO();
    private final UserDAO userDAO = new UserDAO();

    private TabPane tabPane;
    private Tab tabJadwalIbadah;
    private Tab tabJadwalTugas;
    private Tab tabLokasi;

    // ── Jadwal Ibadah ──
    private TableView<JadwalIbadah> tableJadwal;
    private ObservableList<JadwalIbadah> jadwalData;

    // ── Jadwal Tugas ──
    private TableView<JadwalTugas> tableTugas;
    private ObservableList<JadwalTugas> tugasData;

    // ── Lokasi ──
    private TableView<Lokasi> tableLokasi;
    private ObservableList<Lokasi> lokasiData;

    public AdminView(User user) {
        this.currentUser = user;
        buildUI();
        setVisible(false);
        setManaged(false);
    }

    public void setUser(User user) {
        this.currentUser = user;
    }

    public void showJadwalIbadah() {
        if (tabPane != null) tabPane.getSelectionModel().select(tabJadwalIbadah);
    }

    public void showJadwalTugas() {
        if (tabPane != null) tabPane.getSelectionModel().select(tabJadwalTugas);
    }

    public void refresh() {
        loadJadwalIbadah();
        loadJadwalTugas();
        loadLokasi();
    }

    private void buildUI() {
        setStyle("-fx-background-color: #F0F4FF;");
        VBox.setVgrow(this, Priority.ALWAYS);

        // ── Header ──────────────────────────────────────────────────────
        HBox header = new HBox(14);
        header.setPadding(new Insets(18, 24, 18, 24));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: white; -fx-border-color: #E2E8F0; -fx-border-width: 0 0 1 0;");

        javafx.scene.shape.Circle iconCircle = new javafx.scene.shape.Circle(20);
        iconCircle.setFill(Color.web("#5B8DEF"));
        javafx.scene.layout.StackPane iconBox = new javafx.scene.layout.StackPane(iconCircle);
        Text iconText = new Text("⚙");
        iconText.setFill(Color.WHITE);
        iconText.setFont(Font.font("System", FontWeight.BOLD, 14));
        iconBox.getChildren().add(iconText);

        VBox htxt = new VBox(3);
        Text htitle = new Text("Panel Admin");
        htitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        htitle.setFill(Color.web("#1A1F36"));
        Text hsub = new Text("Kelola data jadwal ibadah, jadwal tugas, dan lokasi gereja");
        hsub.setFont(Font.font("System", 12));
        hsub.setFill(Color.web("#8892B0"));
        htxt.getChildren().addAll(htitle, hsub);
        header.getChildren().addAll(iconBox, htxt);

        // ── TabPane ──────────────────────────────────────────────────────
        tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setStyle("-fx-background-color: #F0F4FF;");
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        tabJadwalIbadah = new Tab("📅  Jadwal Ibadah", buildJadwalIbadahTab());
        tabJadwalTugas  = new Tab("📋  Jadwal Tugas",  buildJadwalTugasTab());
        tabLokasi       = new Tab("📍  Lokasi Gereja", buildLokasiTab());

        tabPane.getTabs().addAll(tabJadwalIbadah, tabJadwalTugas, tabLokasi);
        tabPane.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, nv) -> refresh());

        getChildren().addAll(header, tabPane);
        loadJadwalIbadah();
        loadJadwalTugas();
        loadLokasi();
    }

    // ════════════════════════════════════════════════════════════════════
    //  TAB: JADWAL IBADAH
    // ════════════════════════════════════════════════════════════════════

    @SuppressWarnings("unchecked")
    private VBox buildJadwalIbadahTab() {
        VBox box = new VBox(14);
        box.setPadding(new Insets(18));
        box.setStyle("-fx-background-color: #F0F4FF;");

        // ── Form tambah/edit ──
        TitledPane formPane = new TitledPane("Tambah / Edit Jadwal Ibadah", buildJadwalForm());
        formPane.setCollapsible(true);
        formPane.setExpanded(false);
        formPane.setStyle("-fx-font-weight: bold;");

        // ── Table ──
        tableJadwal = new TableView<>();
        tableJadwal.setStyle("-fx-background-radius: 8; -fx-border-radius: 8;");
        jadwalData = FXCollections.observableArrayList();
        tableJadwal.setItems(jadwalData);
        VBox.setVgrow(tableJadwal, Priority.ALWAYS);

        TableColumn<JadwalIbadah, String> colNama = new TableColumn<>("Nama Ibadah");
        colNama.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNamaIbadah()));
        colNama.setPrefWidth(200);

        TableColumn<JadwalIbadah, String> colTgl = new TableColumn<>("Tanggal");
        colTgl.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTanggalStr()));
        colTgl.setPrefWidth(110);

        TableColumn<JadwalIbadah, String> colWkt = new TableColumn<>("Waktu");
        colWkt.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getWaktuStr()));
        colWkt.setPrefWidth(100);

        TableColumn<JadwalIbadah, String> colLok = new TableColumn<>("Lokasi");
        colLok.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getNamaLokasi() != null ? c.getValue().getNamaLokasi() : "-"));
        colLok.setPrefWidth(160);

        TableColumn<JadwalIbadah, Void> colAksi = buildActionColumn(
                "Jadwal Ibadah",
                item -> editJadwalIbadah((JadwalIbadah) item),
                item -> { jadwalIbadahDAO.delete(((JadwalIbadah)item).getIdJadwal()); loadJadwalIbadah(); });

        tableJadwal.getColumns().addAll(colNama, colTgl, colWkt, colLok, colAksi);

        // ── Add button ──
        Button btnAdd = primaryButton("+ Tambah Jadwal Ibadah");
        btnAdd.setOnAction(e -> formPane.setExpanded(!formPane.isExpanded()));

        box.getChildren().addAll(btnAdd, formPane, tableJadwal);
        return box;
    }

    private VBox buildJadwalForm() {
        // Fields
        TextField fNama   = new TextField(); fNama.setPromptText("Nama Ibadah*");
        DatePicker fTgl   = new DatePicker(LocalDate.now());
        TextField fWaktu  = new TextField("07:00"); fWaktu.setPromptText("HH:MM*");
        ComboBox<String> fLokasi = new ComboBox<>();
        fLokasi.setPromptText("Pilih Lokasi*");
        fLokasi.setMaxWidth(Double.MAX_VALUE);
        refreshLokasiCombo(fLokasi);

        GridPane grid = formGrid();
        grid.add(label("Nama Ibadah*"), 0, 0);    grid.add(fNama, 1, 0);
        grid.add(label("Tanggal*"), 0, 1);         grid.add(fTgl, 1, 1);
        grid.add(label("Waktu (HH:MM)*"), 0, 2);  grid.add(fWaktu, 1, 2);
        grid.add(label("Lokasi*"), 0, 3);          grid.add(fLokasi, 1, 3);

        // Highlight empty required field
        Button btnSimpan = primaryButton("💾 Simpan Jadwal");
        Label lblStatus = new Label();

        btnSimpan.setOnAction(e -> {
            boolean valid = validateNotEmpty(lblStatus,
                    fNama.getText(), fWaktu.getText());
            if (fLokasi.getValue() == null) {
                lblStatus.setText("⚠️ Pilih lokasi terlebih dahulu.");
                lblStatus.setTextFill(Color.RED);
                valid = false;
            }
            if (!valid) return;

            try {
                LocalTime wkt = LocalTime.parse(fWaktu.getText().trim());
                JadwalIbadah j = new JadwalIbadah();
                j.setNamaIbadah(fNama.getText().trim());
                j.setTanggal(fTgl.getValue());
                j.setWaktu(wkt);
                j.setIdLokasi(fLokasi.getValue().split("\\|")[0].trim());
                j.setIdUser(currentUser != null ? currentUser.getIdUser() : "");

                if (jadwalIbadahDAO.insert(j)) {
                    lblStatus.setText("✅ Jadwal berhasil disimpan!");
                    lblStatus.setTextFill(Color.GREEN);
                    fNama.clear(); fWaktu.setText("07:00"); fTgl.setValue(LocalDate.now());
                    fLokasi.setValue(null);
                    loadJadwalIbadah();
                }
            } catch (Exception ex) {
                lblStatus.setText("⚠️ Format waktu tidak valid (HH:MM).");
                lblStatus.setTextFill(Color.RED);
            }
        });

        VBox form = new VBox(10, grid, btnSimpan, lblStatus);
        form.setPadding(new Insets(12));
        return form;
    }

    private void editJadwalIbadah(JadwalIbadah item) {
        Dialog<JadwalIbadah> dlg = new Dialog<>();
        dlg.setTitle("Edit Jadwal Ibadah");

        TextField fNama   = new TextField(item.getNamaIbadah());
        DatePicker fTgl   = new DatePicker(item.getTanggal());
        TextField fWaktu  = new TextField(item.getWaktu() != null ? item.getWaktu().toString().substring(0,5) : "");
        ComboBox<String> fLokasi = new ComboBox<>();
        refreshLokasiCombo(fLokasi);
        if (item.getNamaLokasi() != null) fLokasi.setValue(item.getIdLokasi() + " | " + item.getNamaLokasi());

        GridPane grid = formGrid();
        grid.add(label("Nama Ibadah*"), 0, 0);    grid.add(fNama, 1, 0);
        grid.add(label("Tanggal*"), 0, 1);         grid.add(fTgl, 1, 1);
        grid.add(label("Waktu (HH:MM)*"), 0, 2);  grid.add(fWaktu, 1, 2);
        grid.add(label("Lokasi*"), 0, 3);          grid.add(fLokasi, 1, 3);

        dlg.getDialogPane().setContent(grid);
        dlg.getDialogPane().setPrefWidth(400);
        ButtonType ok = new ButtonType("Simpan", ButtonBar.ButtonData.OK_DONE);
        dlg.getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);
        dlg.setResultConverter(b -> {
            if (b == ok) {
                try {
                    item.setNamaIbadah(fNama.getText().trim());
                    item.setTanggal(fTgl.getValue());
                    item.setWaktu(LocalTime.parse(fWaktu.getText().trim()));
                    if (fLokasi.getValue() != null)
                        item.setIdLokasi(fLokasi.getValue().split("\\|")[0].trim());
                } catch (Exception ignored) {}
                return item;
            }
            return null;
        });
        dlg.showAndWait().ifPresent(j -> { jadwalIbadahDAO.update(j); loadJadwalIbadah(); });
    }

    private void loadJadwalIbadah() {
        if (jadwalData == null) return;
        jadwalData.setAll(jadwalIbadahDAO.getAll());
    }

    // ════════════════════════════════════════════════════════════════════
    //  TAB: JADWAL TUGAS
    // ════════════════════════════════════════════════════════════════════

    @SuppressWarnings("unchecked")
    private VBox buildJadwalTugasTab() {
        VBox box = new VBox(14);
        box.setPadding(new Insets(18));
        box.setStyle("-fx-background-color: #F0F4FF;");

        TitledPane formPane = new TitledPane("Tambah / Edit Jadwal Tugas", buildTugasForm());
        formPane.setCollapsible(true);
        formPane.setExpanded(false);

        tableTugas = new TableView<>();
        tugasData = FXCollections.observableArrayList();
        tableTugas.setItems(tugasData);
        VBox.setVgrow(tableTugas, Priority.ALWAYS);

        TableColumn<JadwalTugas, String> colMajelis = new TableColumn<>("Majelis");
        colMajelis.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getNamaMajelis() != null ? c.getValue().getNamaMajelis() : c.getValue().getIdUser()));
        colMajelis.setPrefWidth(160);

        TableColumn<JadwalTugas, String> colTugas = new TableColumn<>("Tugas");
        colTugas.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTugas()));
        colTugas.setPrefWidth(200);

        TableColumn<JadwalTugas, String> colTgl = new TableColumn<>("Tanggal");
        colTgl.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTanggalStr()));
        colTgl.setPrefWidth(110);

        TableColumn<JadwalTugas, String> colWkt = new TableColumn<>("Waktu");
        colWkt.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getWaktuStr()));
        colWkt.setPrefWidth(100);

        TableColumn<JadwalTugas, Void> colAksi = buildActionColumn(
                "Jadwal Tugas",
                item -> editJadwalTugas((JadwalTugas) item),
                item -> { jadwalTugasDAO.delete(((JadwalTugas)item).getIdTugas()); loadJadwalTugas(); });

        tableTugas.getColumns().addAll(colMajelis, colTugas, colTgl, colWkt, colAksi);

        Button btnAdd = primaryButton("+ Tambah Jadwal Tugas");
        btnAdd.setOnAction(e -> formPane.setExpanded(!formPane.isExpanded()));

        box.getChildren().addAll(btnAdd, formPane, tableTugas);
        return box;
    }

    private VBox buildTugasForm() {
        ComboBox<String> fMajelis = new ComboBox<>();
        fMajelis.setPromptText("Pilih Nama Majelis*");
        fMajelis.setMaxWidth(Double.MAX_VALUE);
        refreshMajelisCombo(fMajelis);

        TextField fTugas  = new TextField(); fTugas.setPromptText("Deskripsi tugas*");
        DatePicker fTgl   = new DatePicker(LocalDate.now());
        TextField fWaktu  = new TextField("09:00"); fWaktu.setPromptText("HH:MM");

        GridPane grid = formGrid();
        grid.add(label("Nama Majelis*"), 0, 0);    grid.add(fMajelis, 1, 0);
        grid.add(label("Deskripsi Tugas*"), 0, 1); grid.add(fTugas, 1, 1);
        grid.add(label("Tanggal*"), 0, 2);          grid.add(fTgl, 1, 2);
        grid.add(label("Waktu (HH:MM)"), 0, 3);    grid.add(fWaktu, 1, 3);

        Button btnSimpan = primaryButton("💾 Simpan Jadwal Tugas");
        Label lblStatus  = new Label();

        btnSimpan.setOnAction(e -> {
            if (!validateNotEmpty(lblStatus, fTugas.getText())) return;
            if (fMajelis.getValue() == null) {
                lblStatus.setText("⚠️ Pilih majelis terlebih dahulu.");
                lblStatus.setTextFill(Color.RED); return;
            }
            try {
                JadwalTugas jt = new JadwalTugas();
                jt.setIdUser(fMajelis.getValue().split("\\|")[0].trim());
                jt.setTugas(fTugas.getText().trim());
                jt.setTanggal(fTgl.getValue());
                if (!fWaktu.getText().trim().isEmpty())
                    jt.setWaktuIbadah(LocalTime.parse(fWaktu.getText().trim()));

                if (jadwalTugasDAO.insert(jt)) {
                    lblStatus.setText("✅ Berhasil disimpan!"); lblStatus.setTextFill(Color.GREEN);
                    fTugas.clear(); fWaktu.setText("09:00"); fTgl.setValue(LocalDate.now());
                    fMajelis.setValue(null);
                    loadJadwalTugas();
                }
            } catch (Exception ex) {
                lblStatus.setText("⚠️ Format waktu tidak valid (HH:MM)."); lblStatus.setTextFill(Color.RED);
            }
        });

        VBox form = new VBox(10, grid, btnSimpan, lblStatus);
        form.setPadding(new Insets(12));
        return form;
    }

    private void editJadwalTugas(JadwalTugas item) {
        Dialog<JadwalTugas> dlg = new Dialog<>();
        dlg.setTitle("Edit Jadwal Tugas");

        ComboBox<String> fMajelis = new ComboBox<>();
        refreshMajelisCombo(fMajelis);
        TextField fTugas = new TextField(item.getTugas());
        DatePicker fTgl  = new DatePicker(item.getTanggal());
        TextField fWaktu = new TextField(item.getWaktuStr().replace(" WIB", "").equals("-") ? "" : item.getWaktuStr().replace(" WIB", ""));

        GridPane grid = formGrid();
        grid.add(label("Nama Majelis*"), 0, 0);    grid.add(fMajelis, 1, 0);
        grid.add(label("Deskripsi Tugas*"), 0, 1); grid.add(fTugas, 1, 1);
        grid.add(label("Tanggal*"), 0, 2);          grid.add(fTgl, 1, 2);
        grid.add(label("Waktu (HH:MM)"), 0, 3);    grid.add(fWaktu, 1, 3);

        dlg.getDialogPane().setContent(grid);
        dlg.getDialogPane().setPrefWidth(400);
        ButtonType ok = new ButtonType("Simpan", ButtonBar.ButtonData.OK_DONE);
        dlg.getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);
        dlg.setResultConverter(b -> {
            if (b == ok) {
                item.setTugas(fTugas.getText().trim());
                item.setTanggal(fTgl.getValue());
                if (fMajelis.getValue() != null)
                    item.setIdUser(fMajelis.getValue().split("\\|")[0].trim());
                try {
                    if (!fWaktu.getText().trim().isEmpty())
                        item.setWaktuIbadah(LocalTime.parse(fWaktu.getText().trim()));
                } catch (Exception ignored) {}
                return item;
            }
            return null;
        });
        dlg.showAndWait().ifPresent(jt -> { jadwalTugasDAO.update(jt); loadJadwalTugas(); });
    }

    private void loadJadwalTugas() {
        if (tugasData == null) return;
        tugasData.setAll(jadwalTugasDAO.getAll());
    }

    // ════════════════════════════════════════════════════════════════════
    //  TAB: LOKASI GEREJA
    // ════════════════════════════════════════════════════════════════════

    @SuppressWarnings("unchecked")
    private VBox buildLokasiTab() {
        VBox box = new VBox(14);
        box.setPadding(new Insets(18));
        box.setStyle("-fx-background-color: #F0F4FF;");

        TitledPane formPane = new TitledPane("Tambah / Edit Lokasi", buildLokasiForm());
        formPane.setCollapsible(true);
        formPane.setExpanded(false);

        tableLokasi = new TableView<>();
        lokasiData  = FXCollections.observableArrayList();
        tableLokasi.setItems(lokasiData);
        VBox.setVgrow(tableLokasi, Priority.ALWAYS);

        TableColumn<Lokasi, String> colNama = new TableColumn<>("Nama Tempat");
        colNama.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNamaTempat()));
        colNama.setPrefWidth(180);

        TableColumn<Lokasi, String> colAlamat = new TableColumn<>("Alamat");
        colAlamat.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getAlamat()));
        colAlamat.setPrefWidth(220);

        TableColumn<Lokasi, String> colKontak = new TableColumn<>("Kontak");
        colKontak.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getKontak()));
        colKontak.setPrefWidth(150);

        TableColumn<Lokasi, Void> colAksi = buildActionColumn(
                "Lokasi",
                item -> editLokasi((Lokasi) item),
                item -> { lokasiDAO.delete(((Lokasi)item).getIdLokasi()); loadLokasi(); });

        tableLokasi.getColumns().addAll(colNama, colAlamat, colKontak, colAksi);

        Button btnAdd = primaryButton("+ Tambah Lokasi");
        btnAdd.setOnAction(e -> formPane.setExpanded(!formPane.isExpanded()));
        box.getChildren().addAll(btnAdd, formPane, tableLokasi);
        return box;
    }

    private VBox buildLokasiForm() {
        TextField fNama   = new TextField(); fNama.setPromptText("Nama tempat*");
        TextField fAlamat = new TextField(); fAlamat.setPromptText("Alamat lengkap*");
        TextField fKontak = new TextField(); fKontak.setPromptText("Nomor telepon / kontak");

        GridPane grid = formGrid();
        grid.add(label("Nama Tempat*"), 0, 0); grid.add(fNama, 1, 0);
        grid.add(label("Alamat*"), 0, 1);       grid.add(fAlamat, 1, 1);
        grid.add(label("Kontak"), 0, 2);        grid.add(fKontak, 1, 2);

        Button btnSimpan = primaryButton("💾 Simpan Lokasi");
        Label lblStatus  = new Label();

        btnSimpan.setOnAction(e -> {
            if (!validateNotEmpty(lblStatus, fNama.getText(), fAlamat.getText())) return;
            Lokasi l = new Lokasi();
            l.setNamaTempat(fNama.getText().trim());
            l.setAlamat(fAlamat.getText().trim());
            l.setKontak(fKontak.getText().trim());
            l.setIdUser(currentUser != null ? currentUser.getIdUser() : "");
            if (lokasiDAO.insert(l)) {
                lblStatus.setText("✅ Lokasi berhasil disimpan!"); lblStatus.setTextFill(Color.GREEN);
                fNama.clear(); fAlamat.clear(); fKontak.clear();
                loadLokasi();
            }
        });

        VBox form = new VBox(10, grid, btnSimpan, lblStatus);
        form.setPadding(new Insets(12));
        return form;
    }

    private void editLokasi(Lokasi item) {
        Dialog<Lokasi> dlg = new Dialog<>();
        dlg.setTitle("Edit Lokasi");
        TextField fNama   = new TextField(item.getNamaTempat());
        TextField fAlamat = new TextField(item.getAlamat());
        TextField fKontak = new TextField(item.getKontak());

        GridPane grid = formGrid();
        grid.add(label("Nama Tempat*"), 0, 0); grid.add(fNama, 1, 0);
        grid.add(label("Alamat*"), 0, 1);       grid.add(fAlamat, 1, 1);
        grid.add(label("Kontak"), 0, 2);        grid.add(fKontak, 1, 2);

        dlg.getDialogPane().setContent(grid);
        dlg.getDialogPane().setPrefWidth(400);
        ButtonType ok = new ButtonType("Simpan", ButtonBar.ButtonData.OK_DONE);
        dlg.getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);
        dlg.setResultConverter(b -> {
            if (b == ok) {
                item.setNamaTempat(fNama.getText().trim());
                item.setAlamat(fAlamat.getText().trim());
                item.setKontak(fKontak.getText().trim());
                return item;
            }
            return null;
        });
        dlg.showAndWait().ifPresent(l -> { lokasiDAO.update(l); loadLokasi(); });
    }

    private void loadLokasi() {
        if (lokasiData == null) return;
        lokasiData.setAll(lokasiDAO.getAll());
    }

    // ════════════════════════════════════════════════════════════════════
    //  HELPERS
    // ════════════════════════════════════════════════════════════════════

    @SuppressWarnings("unchecked")
    private <T> TableColumn<T, Void> buildActionColumn(
            String entity,
            java.util.function.Consumer<Object> onEdit,
            java.util.function.Consumer<Object> onDelete) {

        TableColumn<T, Void> col = new TableColumn<>("Aksi");
        col.setPrefWidth(130);
        col.setCellFactory(tc -> new TableCell<>() {
            private final Button btnEdit = new Button("✏️ Edit");
            private final Button btnHapus = new Button("🗑️ Hapus");
            private final HBox box = new HBox(6, btnEdit, btnHapus);
            {
                box.setAlignment(Pos.CENTER);
                btnEdit.setStyle("-fx-background-color: #5B8DEF; -fx-text-fill: white; " +
                        "-fx-background-radius: 6; -fx-font-size: 11; -fx-cursor: hand;");
                btnHapus.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; " +
                        "-fx-background-radius: 6; -fx-font-size: 11; -fx-cursor: hand;");
                btnEdit.setOnAction(e -> onEdit.accept(getTableView().getItems().get(getIndex())));
                btnHapus.setOnAction(e -> {
                    Object item = getTableView().getItems().get(getIndex());
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Konfirmasi Hapus");
                    confirm.setContentText("Hapus data ini?");
                    confirm.showAndWait().filter(r -> r == ButtonType.OK)
                            .ifPresent(r -> onDelete.accept(item));
                });
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : box);
            }
        });
        return col;
    }

    private void refreshLokasiCombo(ComboBox<String> combo) {
        combo.getItems().clear();
        lokasiDAO.getAll().forEach(l ->
                combo.getItems().add(l.getIdLokasi() + " | " + l.getNamaTempat()));
    }

    private void refreshMajelisCombo(ComboBox<String> combo) {
        combo.getItems().clear();
        userDAO.getAllMajelis().forEach(u ->
                combo.getItems().add(u.getIdUser() + " | " + u.getNama()));
    }

    private GridPane formGrid() {
        GridPane g = new GridPane();
        g.setHgap(12); g.setVgap(10);
        ColumnConstraints c0 = new ColumnConstraints(130);
        ColumnConstraints c1 = new ColumnConstraints();
        c1.setHgrow(Priority.ALWAYS);
        g.getColumnConstraints().addAll(c0, c1);
        return g;
    }

    private Label label(String text) {
        Label lbl = new Label(text);
        lbl.setFont(Font.font("System", FontWeight.BOLD, 12));
        lbl.setTextFill(Color.web("#4A5568"));
        return lbl;
    }

    private Button primaryButton(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: linear-gradient(to right, #5B8DEF, #7C5CBF); " +
                     "-fx-text-fill: white; " +
                     "-fx-background-radius: 10; -fx-font-weight: bold; " +
                     "-fx-cursor: hand; -fx-padding: 10 20 10 20; -fx-font-size: 13;");
        btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color: linear-gradient(to right, #3D6FD4, #6A4AAD); " +
                "-fx-text-fill: white; " +
                "-fx-background-radius: 10; -fx-font-weight: bold; " +
                "-fx-cursor: hand; -fx-padding: 10 20 10 20; -fx-font-size: 13;"));
        btn.setOnMouseExited(e -> btn.setStyle(
                "-fx-background-color: linear-gradient(to right, #5B8DEF, #7C5CBF); " +
                "-fx-text-fill: white; " +
                "-fx-background-radius: 10; -fx-font-weight: bold; " +
                "-fx-cursor: hand; -fx-padding: 10 20 10 20; -fx-font-size: 13;"));
        return btn;
    }

    private boolean validateNotEmpty(Label statusLabel, String... fields) {
        for (String f : fields) {
            if (f == null || f.trim().isEmpty()) {
                statusLabel.setText("⚠️ Semua field wajib tidak boleh kosong.");
                statusLabel.setTextFill(Color.RED);
                return false;
            }
        }
        return true;
    }
}
