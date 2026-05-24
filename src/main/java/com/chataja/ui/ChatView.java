package com.chataja.ui;

import com.chataja.chatbot.ChatBot;
import com.chataja.model.Pengumuman;
import com.chataja.model.Renungan;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.*;
import javafx.util.Duration;

import java.io.File;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Panel antarmuka chatbot utama.
 * Desain: dark theme sesuai Figma.
 */
public class ChatView extends VBox {

    private final ChatBot chatBot;
    private VBox messageContainer;
    private ScrollPane scrollPane;
    private TextField inputField;
    private Button sendBtn;

    private static final String BG = "#3B3B3B";
    private static final String CARD = "#484848";
    private static final String INPUT_BG = "#5C5C5C";
    private static final String ACCENT = "#5B8DEF";
    private static final String USER_BG = "#5B8DEF";
    private static final String BOT_BG = "#4A4A4A";

    public ChatView(ChatBot chatBot) {
        this.chatBot = chatBot;
        buildUI();
        addWelcomeMessage();
    }

    private void buildUI() {
        setStyle("-fx-background-color: " + BG + ";");
        VBox.setVgrow(this, Priority.ALWAYS);

        // ── Header ──────────────────────────────────────────────────────
        HBox header = buildPageHeader("Chat bot - jemaat");
        VBox.setMargin(header, new Insets(12, 12, 0, 12));

        // ── Message container ────────────────────────────────────────────
        messageContainer = new VBox(14);
        messageContainer.setPadding(new Insets(20, 24, 20, 24));

        scrollPane = new ScrollPane(messageContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: " + BG + "; -fx-background: " + BG + "; " +
                "-fx-border-width: 0;");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        messageContainer.heightProperty().addListener((obs, old, nv) -> scrollPane.setVvalue(1.0));

        // ── Input area ───────────────────────────────────────────────────
        HBox inputArea = new HBox(10);
        inputArea.setPadding(new Insets(10, 16, 14, 16));
        inputArea.setAlignment(Pos.CENTER);
        inputArea.setStyle("-fx-background-color: " + BG + "; " +
                "-fx-border-color: #555555; -fx-border-width: 1 0 0 0;");

        // Menu button
        Button btnMenu = new Button("☰");
        btnMenu.setFont(Font.font("System", FontWeight.BOLD, 14));
        btnMenu.setPrefSize(42, 42);
        btnMenu.setStyle("-fx-background-color: " + ACCENT + "; -fx-text-fill: white; " +
                "-fx-background-radius: 10; -fx-cursor: hand; -fx-border-width: 0;");
        btnMenu.setOnAction(e -> showQuickMenu());

        // Input wrapper
        HBox inputWrapper = new HBox(0);
        inputWrapper.setAlignment(Pos.CENTER);
        inputWrapper.setStyle("-fx-background-color: " + INPUT_BG + "; " +
                "-fx-background-radius: 10; -fx-border-width: 0;");
        HBox.setHgrow(inputWrapper, Priority.ALWAYS);

        inputField = new TextField();
        inputField.setPromptText("Ajukan pertanyaan...");
        inputField.setStyle("-fx-background-color: transparent; -fx-border-width: 0; " +
                "-fx-padding: 11 14 11 16; -fx-font-size: 13; -fx-text-fill: white; " +
                "-fx-prompt-text-fill: rgba(255,255,255,0.45);");
        inputField.setOnAction(e -> sendMessage(inputField.getText()));
        HBox.setHgrow(inputField, Priority.ALWAYS);
        inputWrapper.getChildren().add(inputField);

        sendBtn = new Button("⮞");
        sendBtn.setFont(Font.font("System", FontWeight.BOLD, 15));
        sendBtn.setPrefSize(42, 42);
        sendBtn.setStyle("-fx-background-color: " + ACCENT + "; -fx-text-fill: white; " +
                "-fx-background-radius: 10; -fx-cursor: hand; -fx-border-width: 0;");
        sendBtn.setOnMouseEntered(e -> sendBtn.setStyle(
                "-fx-background-color: #3D6FD4; -fx-text-fill: white; " +
                        "-fx-background-radius: 10; -fx-cursor: hand; -fx-border-width: 0;"));
        sendBtn.setOnMouseExited(e -> sendBtn.setStyle(
                "-fx-background-color: " + ACCENT + "; -fx-text-fill: white; " +
                        "-fx-background-radius: 10; -fx-cursor: hand; -fx-border-width: 0;"));
        sendBtn.setOnAction(e -> sendMessage(inputField.getText()));

        inputArea.getChildren().addAll(btnMenu, inputWrapper, sendBtn);

        getChildren().addAll(header, scrollPane, inputArea);
    }

    /** Page header with logo + title, dark rounded bar */
    static HBox buildPageHeader(String title) {
        HBox header = new HBox(14);
        header.setPadding(new Insets(14, 20, 14, 20));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: #424242; -fx-background-radius: 12;");
        header.setMaxWidth(Double.MAX_VALUE);

        // Logo icon dari file gambar
        ImageView logoIcon = new ImageView();
        try {
            Image img = new Image(ChatView.class.getResourceAsStream("/logo.png"));
            logoIcon.setImage(img);
            logoIcon.setFitWidth(36);
            logoIcon.setFitHeight(36);
            logoIcon.setPreserveRatio(true);
            logoIcon.setSmooth(true);
        } catch (Exception ignored) {
        }

        Text titleText = new Text(title);
        titleText.setFill(Color.WHITE);
        titleText.setFont(Font.font("System", FontWeight.BOLD, 17));

        Region rightSpacer = new Region();
        HBox.setHgrow(rightSpacer, Priority.ALWAYS);

        header.getChildren().addAll(logoIcon, titleText, rightSpacer);
        return header;
    }

    // ── Messaging ────────────────────────────────────────────────────────

    private void sendMessage(String text) {
        if (text == null || text.trim().isEmpty())
            return;
        String trimmed = text.trim();
        addUserMessage(trimmed);
        inputField.clear();

        HBox typingIndicator = buildTypingIndicator();
        messageContainer.getChildren().add(typingIndicator);

        javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(Duration.millis(700));
        delay.setOnFinished(e -> {
            messageContainer.getChildren().remove(typingIndicator);

            // Jika multi-intent → langsung proses semua intent sekaligus
            if (chatBot.isMultiIntentQuery(trimmed)) {
                String response = chatBot.prosesPertanyaan(trimmed);
                addBotMessage(response);
            }
            // Single-intent: prioritaskan renderer khusus (dengan gambar)
            else if (chatBot.isPengumumanQuery(trimmed)) {
                handlePengumumanResponse();
            } else if (chatBot.isRenunganQuery(trimmed)) {
                handleRenunganResponse();
            } else {
                String response = chatBot.prosesPertanyaan(trimmed);
                addBotMessage(response);
            }
        });
        delay.play();
    }

    private boolean containsKeyword(String text, String... keywords) {
        for (String kw : keywords) {
            if (text.contains(kw))
                return true;
        }
        return false;
    }

    private void handleRenunganResponse() {
        Renungan r = chatBot.getRenunganHariIni();
        if (r != null) {
            String text = r.tampilkan();
            String imagePath = r.hasFoto() ? r.getFotoPath() : null;
            addBotMessage(text, imagePath);
        } else {
            addBotMessage("📖 Renungan harian sedang disiapkan.\nSilakan cek kembali nanti.");
        }
    }

    private void handlePengumumanResponse() {
        List<Pengumuman> list = chatBot.getLatestPengumuman(5);

        if (list.isEmpty()) {
            addBotMessage("📢 Belum ada pengumuman saat ini.");
            return;
        }

        // Tampilkan header
        StringBuilder sb = new StringBuilder();
        String line = ("─".repeat(35));
        sb.append("📢 PENGUMUMAN GEREJA\n");
        sb.append("─".repeat(35)).append("\n\n");

        // Tampilkan setiap pengumuman dengan gambar jika ada
        for (int i = 0; i < list.size(); i++) {
            Pengumuman p = list.get(i);
            String imagePath = p.hasFoto() ? p.getFotoPath() : null;

            String text = p.tampilkan();

            if (i == 0) {
                addBotMessage(sb.toString() + text, imagePath);
            } else {
                addBotMessage(line + "\n" + text, imagePath);
            }
        }
    }

    private void showQuickMenu() {
        ContextMenu menu = new ContextMenu();
        String[] items = { "Jadwal ibadah", "Lokasi gereja", "Renungan hari ini", "Pengumuman gereja" };
        for (String item : items) {
            MenuItem mi = new MenuItem(item);
            mi.setOnAction(e -> sendMessage(item));
            menu.getItems().add(mi);
        }
        menu.show(inputField.getScene().getWindow(),
                inputField.localToScreen(0, 0).getX(),
                inputField.localToScreen(0, 0).getY() - 130);
    }

    public void addUserMessage(String text) {
        HBox wrapper = new HBox();
        wrapper.setAlignment(Pos.CENTER_RIGHT);
        wrapper.setPadding(new Insets(0, 0, 0, 80));

        Label lbl = new Label(text);
        lbl.setWrapText(true);
        lbl.setFont(Font.font("System", 13));
        lbl.setTextFill(Color.WHITE);
        lbl.setStyle("-fx-background-color: " + USER_BG + "; " +
                "-fx-background-radius: 18 18 4 18; -fx-padding: 10 16 10 16;");
        lbl.setMaxWidth(460);

        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.color(0.36, 0.55, 0.94, 0.3));
        shadow.setRadius(8);
        shadow.setOffsetY(2);
        lbl.setEffect(shadow);

        VBox bubble = new VBox(4, lbl);
        Label ts = new Label(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
        ts.setFont(Font.font("System", 10));
        ts.setTextFill(Color.web("#AAAAAA"));
        HBox tsRow = new HBox(ts);
        tsRow.setAlignment(Pos.CENTER_RIGHT);
        bubble.getChildren().add(tsRow);

        wrapper.getChildren().add(bubble);
        animateIn(wrapper, true);
        messageContainer.getChildren().add(wrapper);
    }

    public void addBotMessage(String text) {
        addBotMessage(text, null);
    }

    public void addBotMessage(String text, String imagePath) {
        HBox wrapper = new HBox(12);
        wrapper.setAlignment(Pos.TOP_LEFT);
        wrapper.setPadding(new Insets(0, 80, 0, 0));

        VBox contentBox = new VBox(8);
        contentBox.setMaxWidth(500);
        contentBox.setStyle("-fx-background-color: " + BOT_BG + "; " +
                "-fx-background-radius: 4 18 18 18; -fx-padding: 8;");

        // Text label
        Label lbl = new Label(text);
        lbl.setWrapText(true);
        lbl.setFont(Font.font("System", 13));
        lbl.setTextFill(Color.WHITE);
        lbl.setStyle("-fx-background-color: transparent; -fx-padding: 2 8 6 8;");
        lbl.setMaxWidth(500);

        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.color(0, 0, 0, 0.2));
        shadow.setRadius(8);
        shadow.setOffsetY(2);
        contentBox.setEffect(shadow);

        // Image jika ada
        if (imagePath != null && !imagePath.isBlank()) {
            try {
                File imgFile = new File(imagePath);
                if (imgFile.exists()) {
                    ImageView imageView = new ImageView(new Image(imgFile.toURI().toString()));
                    imageView.setFitWidth(300);
                    imageView.setPreserveRatio(true);
                    imageView.setSmooth(true);
                    imageView.setStyle(
                            "-fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 8, 0, 0, 2);");

                    // Container gambar menyatu dalam bubble yang sama
                    StackPane imageContainer = new StackPane(imageView);
                    imageContainer.setStyle("-fx-background-color: transparent; -fx-padding: 2 4 2 4;");

                    contentBox.getChildren().add(imageContainer);
                }
            } catch (Exception e) {
                System.err.println("Error loading image in chat: " + e.getMessage());
            }
        }

        contentBox.getChildren().add(lbl);

        VBox bubble = new VBox(4, contentBox);
        Label ts = new Label("ChatAja  " + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
        ts.setFont(Font.font("System", 10));
        ts.setTextFill(Color.web("#AAAAAA"));
        bubble.getChildren().add(ts);

        wrapper.getChildren().add(bubble);
        animateIn(wrapper, false);
        messageContainer.getChildren().add(wrapper);
    }

    private void animateIn(HBox node, boolean fromRight) {
        node.setOpacity(0);
        FadeTransition fade = new FadeTransition(Duration.millis(250), node);
        fade.setFromValue(0);
        fade.setToValue(1);
        TranslateTransition slide = new TranslateTransition(Duration.millis(200), node);
        slide.setFromX(fromRight ? 20 : -20);
        slide.setToX(0);
        fade.play();
        slide.play();
    }

    private HBox buildTypingIndicator() {
        HBox wrapper = new HBox(12);
        wrapper.setAlignment(Pos.TOP_LEFT);
        Label typing = new Label("● ● ●");
        typing.setFont(Font.font("System", FontPosture.ITALIC, 13));
        typing.setTextFill(Color.web("#AAAAAA"));
        typing.setStyle("-fx-background-color: " + BOT_BG + "; -fx-background-radius: 4 18 18 18; " +
                "-fx-padding: 10 16 10 16;");
        FadeTransition pulse = new FadeTransition(Duration.millis(600), typing);
        pulse.setFromValue(0.4);
        pulse.setToValue(1.0);
        pulse.setCycleCount(FadeTransition.INDEFINITE);
        pulse.setAutoReverse(true);
        pulse.play();
        wrapper.getChildren().add(typing);
        return wrapper;
    }

    private void addWelcomeMessage() {
        addBotMessage("👋 Halo! Saya ChatAja, asisten informasi gereja Anda.\n\n" +
                "Silakan ketik pertanyaan atau klik tombol Menu:\n" +
                "• \"Jadwal ibadah\"\n" +
                "• \"Lokasi gereja\"\n" +
                "• \"Renungan hari ini\"\n" +
                "• \"Pengumuman gereja\"\n\n" +
                "Ketik \"bantuan\" untuk melihat semua fitur yang tersedia.");
    }

    public void clearChat() {
        messageContainer.getChildren().clear();
        addWelcomeMessage();
    }
}
