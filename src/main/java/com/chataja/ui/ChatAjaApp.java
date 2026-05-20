package com.chataja.ui;

import com.chataja.chatbot.ChatBot;
import com.chataja.dao.UserDAO;
import com.chataja.db.DatabaseManager;
import com.chataja.model.User;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class ChatAjaApp extends Application {

    private User currentUser = null;
    private final ChatBot chatBot = new ChatBot();
    private final UserDAO userDAO = new UserDAO();

    private BorderPane root;
    private VBox sidebar;
    private StackPane contentArea;

    private ChatView    chatView;
    private AdminView   adminView;
    private MajelisView majelisView;

    private Button btnNewChat;
    private Button btnLogin;
    private Button btnNavJadwalIbadah;
    private Button btnNavJadwalTugas;
    private Button btnNavKelolAkun;      // ← BARU
    private Button btnNavPengumuman;
    private Button btnNavRenungan;
    private VBox   navSection;
    private Button activeNavBtn = null;

    // ── Palette ───────────────────────────────────────────────────────
    public static final String COLOR_SIDEBAR         = "#6B9EC4";
    public static final String COLOR_SIDEBAR_BTN     = "rgba(255,255,255,0.18)";
    public static final String COLOR_SIDEBAR_BTN_HEX = "#8AB8D4";
    public static final String COLOR_BG              = "#3B3B3B";
    public static final String COLOR_CARD            = "#484848";
    public static final String COLOR_CARD2           = "#525252";
    public static final String COLOR_INPUT           = "#5C5C5C";
    public static final String COLOR_HEADER_BG       = "#424242";
    public static final String COLOR_ACCENT          = "#5B8DEF";
    public static final String COLOR_ACCENT_DARK     = "#3D6FD4";
    public static final String COLOR_WHITE           = "#FFFFFF";
    public static final String COLOR_TEXT            = "#FFFFFF";
    public static final String COLOR_TEXT_MUTED      = "#AAAAAA";
    public static final String COLOR_DANGER          = "#E74C3C";
    public static final String COLOR_SUCCESS         = "#27AE60";
    public static final String COLOR_DIVIDER         = "#666666";
    public static final String COLOR_TEXT_DARK       = "#FFFFFF";
    public static final String COLOR_TEXT_LIGHT      = "#FFFFFF";
    public static final String COLOR_BORDER          = "#5C5C5C";

    @Override
    public void start(Stage primaryStage) {
        DatabaseManager.initialize();
        buildUI(primaryStage);
        primaryStage.show();
        Platform.runLater(this::showWelcomePopup);
    }

    @Override
    public void stop() {
        DatabaseManager.close();
    }

    private void buildUI(Stage stage) {
        root = new BorderPane();
        root.setStyle("-fx-background-color: " + COLOR_BG + ";");

        chatView    = new ChatView(chatBot);
        adminView   = new AdminView(currentUser);
        majelisView = new MajelisView(currentUser, chatBot);

        contentArea = new StackPane();
        contentArea.setStyle("-fx-background-color: " + COLOR_BG + ";");
        contentArea.getChildren().addAll(chatView, adminView, majelisView);
        showChatView();

        sidebar = buildSidebar();

        DropShadow sidebarShadow = new DropShadow();
        sidebarShadow.setColor(Color.color(0, 0, 0, 0.3));
        sidebarShadow.setRadius(16);
        sidebarShadow.setOffsetX(4);
        sidebar.setEffect(sidebarShadow);

        root.setLeft(sidebar);
        root.setCenter(contentArea);

        Scene scene = new Scene(root, 960, 680);
        stage.setScene(scene);
        stage.setTitle("ChatAja – Informasi Gereja");
        stage.setMinWidth(750);
        stage.setMinHeight(520);
        stage.setOnCloseRequest(e -> Platform.exit());
    }

    // ── Sidebar ───────────────────────────────────────────────────────

    private VBox buildSidebar() {
        VBox sb = new VBox(0);
        sb.setPrefWidth(230);
        sb.setStyle("-fx-background-color: " + COLOR_SIDEBAR + ";");

        // ── User info (hidden until login) ──
        VBox userInfoBox = new VBox(6);
        userInfoBox.setPadding(new Insets(14, 16, 14, 16));
        userInfoBox.setAlignment(Pos.CENTER_LEFT);
        userInfoBox.setVisible(false);
        userInfoBox.setManaged(false);
        userInfoBox.setId("userInfoBox");

        Region userDivider = new Region();
        userDivider.setPrefHeight(1);
        userDivider.setStyle("-fx-background-color: rgba(255,255,255,0.25);");
        VBox userDivBox = new VBox(userDivider);
        userDivBox.setPadding(new Insets(0, 16, 0, 16));
        userDivBox.setVisible(false);
        userDivBox.setManaged(false);
        userDivBox.setId("userDivBox");

        // ── Logo ──
        VBox logoBox = new VBox(4);
        logoBox.setPadding(new Insets(22, 16, 16, 16));
        logoBox.setAlignment(Pos.CENTER_LEFT);
        HBox logoRow = new HBox(12);
        logoRow.setAlignment(Pos.CENTER_LEFT);
        ImageView logoIcon = buildLogoIcon();
        Text appName = new Text("ChatAja");
        appName.setFill(javafx.scene.paint.Color.WHITE);
        appName.setFont(javafx.scene.text.Font.font("System", javafx.scene.text.FontWeight.BOLD, 16));
        logoRow.getChildren().addAll(logoIcon, appName);
        logoBox.getChildren().add(logoRow);

        // ── Top divider ──
        Region div1 = new Region();
        div1.setPrefHeight(1);
        div1.setStyle("-fx-background-color: rgba(255,255,255,0.25);");
        VBox div1Box = new VBox(div1);
        div1Box.setPadding(new Insets(0, 16, 0, 16));

        // ── New Chat ──
        btnNewChat = sidebarButton("✦  New Chat", false);
        btnNewChat.setOnAction(e -> {
            chatView.clearChat();
            showChatView();
            setActiveNav(btnNewChat);
        });
        VBox newChatBox = new VBox(btnNewChat);
        newChatBox.setPadding(new Insets(16, 12, 8, 12));

        // ── Nav section (shown after login) ──
        navSection = new VBox(6);
        navSection.setPadding(new Insets(4, 12, 6, 12));
        navSection.setVisible(false);
        navSection.setManaged(false);

        // Nav buttons
        btnNavJadwalIbadah = sidebarButton("📅  Jadwal Ibadah", false);
        btnNavJadwalTugas  = sidebarButton("📋  Jadwal Tugas",  false);
        btnNavKelolAkun    = sidebarButton("👤  Kelola Akun",   false);  // ← BARU
        btnNavPengumuman   = sidebarButton("📢  Pengumuman",    false);
        btnNavRenungan     = sidebarButton("📖  Renungan",      false);

        btnNavJadwalIbadah.setOnAction(e -> {
            adminView.showJadwalIbadah(); showAdminView(); setActiveNav(btnNavJadwalIbadah);
        });
        btnNavJadwalTugas.setOnAction(e -> {
            adminView.showJadwalTugas(); showAdminView(); setActiveNav(btnNavJadwalTugas);
        });
        btnNavKelolAkun.setOnAction(e -> {                             // ← BARU
            adminView.showKelolAkun(); showAdminView(); setActiveNav(btnNavKelolAkun);
        });
        btnNavPengumuman.setOnAction(e -> {
            majelisView.showPengumuman(); showMajelisView(); setActiveNav(btnNavPengumuman);
        });
        btnNavRenungan.setOnAction(e -> {
            majelisView.showRenungan(); showMajelisView(); setActiveNav(btnNavRenungan);
        });

        // Chat always shown after login
        Button btnNavChat = sidebarButton("💬  Chat", false);
        btnNavChat.setOnAction(e -> { showChatView(); setActiveNav(btnNavChat); });
        navSection.getChildren().add(btnNavChat);

        // ── Spacer ──
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // ── Bottom: Login / Logout ──
        VBox bottomArea = new VBox(8);
        bottomArea.setPadding(new Insets(8, 12, 20, 12));
        btnLogin = sidebarButton("🔑  Login", true);
        btnLogin.setOnAction(e -> handleLoginLogout());
        bottomArea.getChildren().add(btnLogin);

        sb.getChildren().addAll(
                userInfoBox, userDivBox,
                logoBox, div1Box,
                newChatBox, navSection,
                spacer, bottomArea);
        return sb;
    }

    private ImageView buildLogoIcon() {
        try {
            Image img = new Image(getClass().getResourceAsStream("/logo.png"));
            ImageView iv = new ImageView(img);
            iv.setFitWidth(48); iv.setFitHeight(48);
            iv.setPreserveRatio(true); iv.setSmooth(true);
            return iv;
        } catch (Exception e) {
            return new ImageView();
        }
    }

    private Button sidebarButton(String text, boolean isLogin) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setPadding(new Insets(10, 16, 10, 16));
        btn.setFont(Font.font("System", FontWeight.SEMI_BOLD, 13));

        String normalStyle = "-fx-background-color: rgba(255,255,255,0.12); " +
                "-fx-text-fill: white; -fx-background-radius: 10; " +
                "-fx-cursor: hand; -fx-border-width: 0;";
        String hoverStyle  = "-fx-background-color: rgba(255,255,255,0.25); " +
                "-fx-text-fill: white; -fx-background-radius: 10; " +
                "-fx-cursor: hand; -fx-border-width: 0;";
        String activeStyle = "-fx-background-color: rgba(255,255,255,0.35); " +
                "-fx-text-fill: white; -fx-background-radius: 10; " +
                "-fx-cursor: hand; -fx-border-width: 0; -fx-font-weight: bold;";

        btn.setStyle(normalStyle);
        btn.setUserData(new String[]{normalStyle, hoverStyle, activeStyle});
        btn.setOnMouseEntered(e -> { if (btn != activeNavBtn) btn.setStyle(hoverStyle); });
        btn.setOnMouseExited(e  -> { if (btn != activeNavBtn) btn.setStyle(normalStyle); });
        return btn;
    }

    private void setActiveNav(Button btn) {
        if (activeNavBtn != null && activeNavBtn != btnLogin) {
            Object data = activeNavBtn.getUserData();
            if (data instanceof String[]) activeNavBtn.setStyle(((String[]) data)[0]);
        }
        activeNavBtn = btn;
        if (btn != null && btn != btnLogin) {
            Object data = btn.getUserData();
            if (data instanceof String[]) btn.setStyle(((String[]) data)[2]);
        }
    }

    // ── View switching ─────────────────────────────────────────────────

    private void showChatView() {
        chatView   .setVisible(true);  chatView.setManaged(true);
        adminView  .setVisible(false); adminView.setManaged(false);
        majelisView.setVisible(false); majelisView.setManaged(false);
    }

    private void showAdminView() {
        adminView.refresh();
        chatView   .setVisible(false); chatView.setManaged(false);
        adminView  .setVisible(true);  adminView.setManaged(true);
        majelisView.setVisible(false); majelisView.setManaged(false);
    }

    private void showMajelisView() {
        majelisView.refresh();
        chatView   .setVisible(false); chatView.setManaged(false);
        adminView  .setVisible(false); adminView.setManaged(false);
        majelisView.setVisible(true);  majelisView.setManaged(true);
    }

    // ── Login / Logout ─────────────────────────────────────────────────

    private void handleLoginLogout() {
        if (currentUser != null) {
            currentUser = null;
            chatBot.setLoggedUser(null);
            updateAfterLogout();
        } else {
            LoginDialog dlg = new LoginDialog();
            dlg.showAndWait().ifPresent(result -> {
                User user = userDAO.login(result[0], result[1], result[2]);
                if (user != null) {
                    currentUser = user;
                    chatBot.setLoggedUser(user);
                    updateAfterLogin(user);
                } else {
                    showAlert(Alert.AlertType.ERROR, "Login Gagal",
                            "Username, password, atau tipe pengguna tidak sesuai.");
                }
            });
        }
    }

    private void updateAfterLogin(User user) {
        // ── User info box ──
        VBox userInfoBox = (VBox) sidebar.lookup("#userInfoBox");
        VBox userDivBox  = (VBox) sidebar.lookup("#userDivBox");
        if (userInfoBox != null) {
            userInfoBox.getChildren().clear();
            HBox userRow = new HBox(12);
            userRow.setAlignment(Pos.CENTER_LEFT);

            StackPane avatar = new StackPane();
            Circle avatarCircle = new Circle(20);
            avatarCircle.setFill(Color.web("#2B6CB0"));
            avatarCircle.setStroke(Color.web("rgba(255,255,255,0.4)"));
            avatarCircle.setStrokeWidth(2);
            String initial = user.getNama() != null && !user.getNama().isEmpty()
                    ? String.valueOf(user.getNama().charAt(0)).toUpperCase() : "U";
            Text avatarText = new Text(initial);
            avatarText.setFill(Color.WHITE);
            avatarText.setFont(Font.font("System", FontWeight.BOLD, 16));
            avatar.getChildren().addAll(avatarCircle, avatarText);

            VBox userMeta = new VBox(2);
            Text userName = new Text(user.getNama());
            userName.setFill(Color.WHITE);
            userName.setFont(Font.font("System", FontWeight.BOLD, 13));
            String roleStr = user.getRole().substring(0, 1).toUpperCase() + user.getRole().substring(1);
            Text userRole = new Text(roleStr);
            userRole.setFill(Color.web("rgba(255,255,255,0.7)"));
            userRole.setFont(Font.font("System", 11));
            userMeta.getChildren().addAll(userName, userRole);

            userRow.getChildren().addAll(avatar, userMeta);
            userInfoBox.getChildren().add(userRow);
            userInfoBox.setVisible(true); userInfoBox.setManaged(true);
        }
        if (userDivBox != null) { userDivBox.setVisible(true); userDivBox.setManaged(true); }

        // ── Login → Log Out button ──
        btnLogin.setText("🚪  Log Out");
        btnLogin.setStyle("-fx-background-color: rgba(231,76,60,0.15); -fx-text-fill: #E74C3C; " +
                "-fx-background-radius: 10; -fx-cursor: hand; -fx-border-width: 0; " +
                "-fx-font-weight: bold; -fx-font-size: 13;");
        btnLogin.setOnMouseEntered(e -> btnLogin.setStyle(
                "-fx-background-color: rgba(231,76,60,0.30); -fx-text-fill: #E74C3C; " +
                        "-fx-background-radius: 10; -fx-cursor: hand; -fx-border-width: 0; -fx-font-weight: bold;"));
        btnLogin.setOnMouseExited(e -> btnLogin.setStyle(
                "-fx-background-color: rgba(231,76,60,0.15); -fx-text-fill: #E74C3C; " +
                        "-fx-background-radius: 10; -fx-cursor: hand; -fx-border-width: 0; -fx-font-weight: bold;"));

        // ── Nav section ──
        navSection.setVisible(true); navSection.setManaged(true);

        // Clear role-specific buttons (keep Chat at index 0)
        while (navSection.getChildren().size() > 1) navSection.getChildren().remove(1);

        if ("admin".equals(user.getRole())) {
            // Admin: Jadwal Ibadah, Jadwal Tugas, Kelola Akun
            navSection.getChildren().addAll(
                    btnNavJadwalIbadah,
                    btnNavJadwalTugas,
                    btnNavKelolAkun       // ← BARU
            );
            adminView.setUser(user);

        } else if ("majelis".equals(user.getRole())) {
            // Majelis: Pengumuman, Renungan
            navSection.getChildren().addAll(btnNavPengumuman, btnNavRenungan);
            majelisView.setUser(user);
        }

        chatView.addBotMessage("✅ Selamat datang, " + user.getNama() + "!\n" +
                "Anda login sebagai " +
                user.getRole().substring(0, 1).toUpperCase() + user.getRole().substring(1) + ".\n\n" +
                ("majelis".equals(user.getRole())
                        ? "Gunakan menu di sebelah kiri untuk mengelola Pengumuman & Renungan.\n" +
                        "Atau tanyakan jadwal tugas Anda langsung di sini!"
                        : "Gunakan menu di sebelah kiri untuk mengelola data sistem."));
    }

    private void updateAfterLogout() {
        VBox userInfoBox = (VBox) sidebar.lookup("#userInfoBox");
        VBox userDivBox  = (VBox) sidebar.lookup("#userDivBox");
        if (userInfoBox != null) { userInfoBox.setVisible(false); userInfoBox.setManaged(false); }
        if (userDivBox  != null) { userDivBox.setVisible(false);  userDivBox.setManaged(false); }

        btnLogin.setText("🔑  Login");
        String normalStyle = "-fx-background-color: rgba(255,255,255,0.12); " +
                "-fx-text-fill: white; -fx-background-radius: 10; " +
                "-fx-cursor: hand; -fx-border-width: 0; -fx-font-weight: bold; -fx-font-size: 13;";
        btnLogin.setStyle(normalStyle);
        btnLogin.setOnMouseEntered(e -> btnLogin.setStyle(
                "-fx-background-color: rgba(255,255,255,0.25); -fx-text-fill: white; " +
                        "-fx-background-radius: 10; -fx-cursor: hand; -fx-border-width: 0; -fx-font-weight: bold;"));
        btnLogin.setOnMouseExited(e -> btnLogin.setStyle(normalStyle));

        navSection.setVisible(false); navSection.setManaged(false);
        setActiveNav(null);
        showChatView();
        chatView.addBotMessage("👋 Anda telah logout. Sampai jumpa!");
    }

    // ── Welcome popup ──────────────────────────────────────────────────

    private void showWelcomePopup() {
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setTitle("Selamat Datang di ChatAja");
        alert.setHeaderText(null);

        VBox content = new VBox(0);
        content.setAlignment(Pos.TOP_CENTER);

        VBox headerBox = new VBox(10);
        headerBox.setAlignment(Pos.CENTER);
        headerBox.setPadding(new Insets(28, 30, 22, 30));
        headerBox.setStyle("-fx-background-color: " + COLOR_SIDEBAR + ";");

        ImageView logoImg = new ImageView();
        try {
            Image logoImage = new Image(getClass().getResourceAsStream("/logo.png"));
            logoImg.setImage(logoImage);
            logoImg.setFitWidth(160); logoImg.setFitHeight(80);
            logoImg.setPreserveRatio(true); logoImg.setSmooth(true);
        } catch (Exception ignored) {}

        Text title = new Text("Selamat Datang di ChatAja!");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));
        title.setFill(Color.WHITE);

        Text sub = new Text("Asisten informasi gereja Anda");
        sub.setFont(Font.font("System", 13));
        sub.setFill(Color.web("rgba(255,255,255,0.75)"));

        headerBox.getChildren().addAll(logoImg, title, sub);

        VBox body = new VBox(12);
        body.setPadding(new Insets(20, 30, 10, 30));
        body.setStyle("-fx-background-color: " + COLOR_CARD + ";");

        String[] features = {
                "📅  Jadwal ibadah & kegiatan",
                "📍  Lokasi & kontak gereja",
                "📖  Renungan harian",
                "📢  Pengumuman gereja"
        };
        for (String f : features) {
            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(8, 14, 8, 14));
            row.setStyle("-fx-background-color: " + COLOR_INPUT + "; -fx-background-radius: 8;");
            Label lbl = new Label(f);
            lbl.setFont(Font.font("System", 13));
            lbl.setTextFill(Color.WHITE);
            row.getChildren().add(lbl);
            body.getChildren().add(row);
        }

        content.getChildren().addAll(headerBox, body);
        alert.getDialogPane().setContent(content);
        alert.getDialogPane().setPrefWidth(400);
        alert.getDialogPane().setStyle("-fx-background-color: " + COLOR_CARD + "; -fx-padding: 0;");

        ButtonType btnClose = new ButtonType("Mulai Chat ✨", ButtonBar.ButtonData.OK_DONE);
        alert.getButtonTypes().add(btnClose);
        alert.getDialogPane().lookupButton(btnClose).setStyle(
                "-fx-background-color: " + COLOR_ACCENT + "; -fx-text-fill: white; " +
                        "-fx-font-weight: bold; -fx-background-radius: 8; " +
                        "-fx-padding: 10 24 10 24; -fx-font-size: 13;");
        alert.showAndWait();
    }

    public static void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
