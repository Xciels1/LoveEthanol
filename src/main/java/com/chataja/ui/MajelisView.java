package com.chataja.ui;

import com.chataja.chatbot.ChatBot;
import com.chataja.dao.PengumumanDAO;
import com.chataja.dao.RenunganDAO;
import com.chataja.model.Pengumuman;
import com.chataja.model.Renungan;
import com.chataja.model.User;
import javafx.beans.property.SimpleStringProperty;
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

/**
 * Panel manajemen untuk Majelis Gereja.
 * UC-5: Kelola Pengumuman (tambah/edit/hapus)
 * UC-5: Kelola Renungan Harian (tambah/edit/hapus)
 */
public class MajelisView extends VBox {

    private User currentUser;
    private final ChatBot chatBot;
    private final PengumumanDAO pengumumanDAO = new PengumumanDAO();
    private final RenunganDAO renunganDAO = new RenunganDAO();

    private TabPane tabPane;
    private Tab tabPengumuman;
    private Tab tabRenungan;

    // ── Pengumuman ──
    private TableView<Pengumuman> tablePengumuman;
    private ObservableList<Pengumuman> pengumumanData;

    // ── Renungan ──
    private TableView<Renungan> tableRenungan;
    private ObservableList<Renungan> renunganData;

    public MajelisView(User user, ChatBot chatBot) {
        this.currentUser = user;
        this.chatBot = chatBot;
        buildUI();
        setVisible(false);
        setManaged(false);
    }

    public void setUser(User user) {
        this.currentUser = user;
    }

    public void showPengumuman() {
        if (tabPane != null) tabPane.getSelectionModel().select(tabPengumuman);
    }

    public void showRenungan() {
        if (tabPane != null) tabPane.getSelectionModel().select(tabRenungan);
    }

    public void refresh() {
        loadPengumuman();
        loadRenungan();
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
        iconCircle.setFill(Color.web("#7C5CBF"));
        javafx.scene.layout.StackPane iconBox = new javafx.scene.layout.StackPane(iconCircle);
        Text iconText = new Text("✍");
        iconText.setFill(Color.WHITE);
        iconText.setFont(Font.font("System", FontWeight.BOLD, 14));
        iconBox.getChildren().add(iconText);

        VBox htxt = new VBox(3);
        Text htitle = new Text("Panel Majelis");
        htitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        htitle.setFill(Color.web("#1A1F36"));
        Text hsub = new Text("Kelola pengumuman dan renungan harian gereja");
        hsub.setFont(Font.font("System", 12));
        hsub.setFill(Color.web("#8892B0"));
        htxt.getChildren().addAll(htitle, hsub);
        header.getChildren().addAll(iconBox, htxt);

        // ── TabPane ──────────────────────────────────────────────────────
        tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        tabPengumuman = new Tab("📢  Pengumuman", buildPengumumanTab());
        tabRenungan   = new Tab("📖  Renungan Harian", buildRenunganTab());

        tabPane.getTabs().addAll(tabPengumuman, tabRenungan);
        tabPane.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, nv) -> refresh());

        getChildren().addAll(header, tabPane);
        loadPengumuman();
        loadRenungan();
    }

    // ════════════════════════════════════════════════════════════════════
    //  TAB: PENGUMUMAN
    // ════════════════════════════════════════════════════════════════════

    @SuppressWarnings("unchecked")
    private VBox buildPengumumanTab() {
        VBox box = new VBox(14);
        box.setPadding(new Insets(18));
        box.setStyle("-fx-background-color: #F0F4FF;");

        // ── Form ──
        TitledPane formPane = new TitledPane("Tambah / Edit Pengumuman", buildPengumumanForm());
        formPane.setCollapsible(true);
        formPane.setExpanded(false);
        formPane.setStyle("-fx-font-weight: bold;");

        // ── Table ──
        tablePengumuman = new TableView<>();
        pengumumanData  = FXCollections.observableArrayList();
        tablePengumuman.setItems(pengumumanData);
        VBox.setVgrow(tablePengumuman, Priority.ALWAYS);

        TableColumn<Pengumuman, String> colJudul = new TableColumn<>("Judul");
        colJudul.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getJudul()));
        colJudul.setPrefWidth(200);

        TableColumn<Pengumuman, String> colIsi = new TableColumn<>("Isi");
        colIsi.setCellValueFactory(c -> {
            String isi = c.getValue().getIsi();
            return new SimpleStringProperty(isi != null && isi.length() > 60
                    ? isi.substring(0, 60) + "..." : isi);
        });
        colIsi.setPrefWidth(250);

        TableColumn<Pengumuman, String> colTgl = new TableColumn<>("Tanggal");
        colTgl.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTanggalStr()));
        colTgl.setPrefWidth(110);

        TableColumn<Pengumuman, Void> colAksi = buildActionColumn(
                item -> editPengumuman((Pengumuman) item),
                item -> { pengumumanDAO.delete(((Pengumuman)item).getIdPengumuman()); loadPengumuman(); });

        tablePengumuman.getColumns().addAll(colJudul, colIsi, colTgl, colAksi);

        Button btnAdd = primaryButton("+ Tambah Pengumuman");
        btnAdd.setOnAction(e -> formPane.setExpanded(!formPane.isExpanded()));

        box.getChildren().addAll(btnAdd, formPane, tablePengumuman);
        return box;
    }

    private VBox buildPengumumanForm() {
        TextField fJudul = new TextField();
        fJudul.setPromptText("Judul pengumuman*");

        TextArea fIsi = new TextArea();
        fIsi.setPromptText("Isi pengumuman*");
        fIsi.setPrefRowCount(4);
        fIsi.setWrapText(true);

        DatePicker fTgl = new DatePicker(LocalDate.now());

        GridPane grid = formGrid();
        grid.add(label("Judul*"), 0, 0);   grid.add(fJudul, 1, 0);
        grid.add(label("Isi*"), 0, 1);     grid.add(fIsi, 1, 1);
        grid.add(label("Tanggal*"), 0, 2); grid.add(fTgl, 1, 2);

        Button btnSimpan = primaryButton("💾 Tambahkan Pengumuman");
        Label lblStatus  = new Label();

        btnSimpan.setOnAction(e -> {
            // Validasi: highlight field kosong dengan warna merah
            boolean valid = true;
            if (fJudul.getText().trim().isEmpty()) {
                fJudul.setStyle(redFieldStyle());
                valid = false;
            } else fJudul.setStyle(normalFieldStyle());

            if (fIsi.getText().trim().isEmpty()) {
                fIsi.setStyle(redFieldStyle());
                valid = false;
            } else fIsi.setStyle(normalFieldStyle());

            if (!valid) {
                lblStatus.setText("⚠️ Field yang ditandai merah wajib diisi.");
                lblStatus.setTextFill(Color.RED);
                return;
            }

            Pengumuman p = new Pengumuman();
            p.setJudul(fJudul.getText().trim());
            p.setIsi(fIsi.getText().trim());
            p.setTanggal(fTgl.getValue());
            p.setIdUser(currentUser != null ? currentUser.getIdUser() : "");

            if (pengumumanDAO.insert(p)) {
                lblStatus.setText("✅ Pengumuman berhasil ditambahkan!");
                lblStatus.setTextFill(Color.GREEN);
                fJudul.clear(); fIsi.clear(); fTgl.setValue(LocalDate.now());
                loadPengumuman();
            }
        });

        VBox form = new VBox(10, grid, btnSimpan, lblStatus);
        form.setPadding(new Insets(12));
        return form;
    }

    private void editPengumuman(Pengumuman item) {
        Dialog<Pengumuman> dlg = new Dialog<>();
        dlg.setTitle("Edit Pengumuman");

        TextField fJudul = new TextField(item.getJudul());
        TextArea  fIsi   = new TextArea(item.getIsi()); fIsi.setPrefRowCount(5); fIsi.setWrapText(true);
        DatePicker fTgl  = new DatePicker(item.getTanggal());

        GridPane grid = formGrid();
        grid.add(label("Judul*"), 0, 0);   grid.add(fJudul, 1, 0);
        grid.add(label("Isi*"), 0, 1);     grid.add(fIsi, 1, 1);
        grid.add(label("Tanggal*"), 0, 2); grid.add(fTgl, 1, 2);

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
        dlg.showAndWait().ifPresent(p -> { pengumumanDAO.update(p); loadPengumuman(); });
    }

    private void loadPengumuman() {
        if (pengumumanData == null) return;
        pengumumanData.setAll(pengumumanDAO.getAll());
    }

    // ════════════════════════════════════════════════════════════════════
    //  TAB: RENUNGAN HARIAN
    // ════════════════════════════════════════════════════════════════════

    @SuppressWarnings("unchecked")
    private VBox buildRenunganTab() {
        VBox box = new VBox(14);
        box.setPadding(new Insets(18));
        box.setStyle("-fx-background-color: #F0F4FF;");

        TitledPane formPane = new TitledPane("Tambah / Edit Renungan", buildRenunganForm());
        formPane.setCollapsible(true);
        formPane.setExpanded(false);
        formPane.setStyle("-fx-font-weight: bold;");

        tableRenungan = new TableView<>();
        renunganData  = FXCollections.observableArrayList();
        tableRenungan.setItems(renunganData);
        VBox.setVgrow(tableRenungan, Priority.ALWAYS);

        TableColumn<Renungan, String> colJudul = new TableColumn<>("Judul");
        colJudul.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getJudul()));
        colJudul.setPrefWidth(200);

        TableColumn<Renungan, String> colIsi = new TableColumn<>("Isi");
        colIsi.setCellValueFactory(c -> {
            String isi = c.getValue().getIsi();
            return new SimpleStringProperty(isi != null && isi.length() > 60
                    ? isi.substring(0, 60) + "..." : isi);
        });
        colIsi.setPrefWidth(250);

        TableColumn<Renungan, String> colTgl = new TableColumn<>("Tanggal");
        colTgl.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTanggalStr()));
        colTgl.setPrefWidth(110);

        TableColumn<Renungan, Void> colAksi = buildActionColumn(
                item -> editRenungan((Renungan) item),
                item -> { renunganDAO.delete(((Renungan)item).getIdRenungan()); loadRenungan(); });

        tableRenungan.getColumns().addAll(colJudul, colIsi, colTgl, colAksi);

        Button btnAdd = primaryButton("+ Tambah Renungan");
        btnAdd.setOnAction(e -> formPane.setExpanded(!formPane.isExpanded()));

        box.getChildren().addAll(btnAdd, formPane, tableRenungan);
        return box;
    }

    private VBox buildRenunganForm() {
        TextField fJudul = new TextField();
        fJudul.setPromptText("Judul renungan*");

        TextArea fIsi = new TextArea();
        fIsi.setPromptText("Isi renungan harian*");
        fIsi.setPrefRowCount(5);
        fIsi.setWrapText(true);

        DatePicker fTgl = new DatePicker(LocalDate.now());

        GridPane grid = formGrid();
        grid.add(label("Judul*"), 0, 0);          grid.add(fJudul, 1, 0);
        grid.add(label("Isi Renungan*"), 0, 1);   grid.add(fIsi, 1, 1);
        grid.add(label("Tanggal*"), 0, 2);         grid.add(fTgl, 1, 2);

        Button btnSimpan = primaryButton("💾 Tambahkan Renungan");
        Label lblStatus  = new Label();

        btnSimpan.setOnAction(e -> {
            boolean valid = true;
            if (fJudul.getText().trim().isEmpty()) {
                fJudul.setStyle(redFieldStyle()); valid = false;
            } else fJudul.setStyle(normalFieldStyle());

            if (fIsi.getText().trim().isEmpty()) {
                fIsi.setStyle(redFieldStyle()); valid = false;
            } else fIsi.setStyle(normalFieldStyle());

            if (!valid) {
                lblStatus.setText("⚠️ Field yang ditandai merah wajib diisi.");
                lblStatus.setTextFill(Color.RED);
                return;
            }

            Renungan r = new Renungan();
            r.setJudul(fJudul.getText().trim());
            r.setIsi(fIsi.getText().trim());
            r.setTanggal(fTgl.getValue());
            r.setIdUser(currentUser != null ? currentUser.getIdUser() : "");

            if (renunganDAO.insert(r)) {
                lblStatus.setText("✅ Renungan berhasil ditambahkan!");
                lblStatus.setTextFill(Color.GREEN);
                fJudul.clear(); fIsi.clear(); fTgl.setValue(LocalDate.now());
                loadRenungan();
            }
        });

        VBox form = new VBox(10, grid, btnSimpan, lblStatus);
        form.setPadding(new Insets(12));
        return form;
    }

    private void editRenungan(Renungan item) {
        Dialog<Renungan> dlg = new Dialog<>();
        dlg.setTitle("Edit Renungan");

        TextField fJudul = new TextField(item.getJudul());
        TextArea  fIsi   = new TextArea(item.getIsi()); fIsi.setPrefRowCount(6); fIsi.setWrapText(true);
        DatePicker fTgl  = new DatePicker(item.getTanggal());

        GridPane grid = formGrid();
        grid.add(label("Judul*"), 0, 0);          grid.add(fJudul, 1, 0);
        grid.add(label("Isi Renungan*"), 0, 1);   grid.add(fIsi, 1, 1);
        grid.add(label("Tanggal*"), 0, 2);         grid.add(fTgl, 1, 2);

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
        dlg.showAndWait().ifPresent(r -> { renunganDAO.update(r); loadRenungan(); });
    }

    private void loadRenungan() {
        if (renunganData == null) return;
        renunganData.setAll(renunganDAO.getAll());
    }

    // ════════════════════════════════════════════════════════════════════
    //  HELPERS
    // ════════════════════════════════════════════════════════════════════

    @SuppressWarnings("unchecked")
    private <T> TableColumn<T, Void> buildActionColumn(
            java.util.function.Consumer<Object> onEdit,
            java.util.function.Consumer<Object> onDelete) {

        TableColumn<T, Void> col = new TableColumn<>("Aksi");
        col.setPrefWidth(130);
        col.setCellFactory(tc -> new TableCell<>() {
            private final Button btnEdit  = new Button("✏️ Edit");
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
                    confirm.setContentText("Yakin ingin menghapus data ini?");
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

    private String redFieldStyle() {
        return "-fx-border-color: #E74C3C; -fx-border-radius: 6; -fx-background-radius: 6;";
    }

    private String normalFieldStyle() {
        return "-fx-border-color: #CBD5E0; -fx-border-radius: 6; -fx-background-radius: 6;";
    }
}
