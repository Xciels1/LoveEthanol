package com.chataja.ui;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;

/**
 * Dialog login untuk Admin dan Majelis.
 * Desain: dark modal sesuai Figma.
 */
public class LoginDialog extends Dialog<String[]> {

    private TextField usernameField;
    private PasswordField passwordField;
    private ComboBox<String> roleCombo;

    private static final String BG       = "#3B3B3B";
    private static final String CARD     = "#484848";
    private static final String INPUT_BG = "#5C5C5C";
    private static final String ACCENT   = "#5B8DEF";

    public LoginDialog() {
        setTitle("Login – ChatAja");
        setHeaderText(null);
        buildContent();
        setupResultConverter();
        getDialogPane().setStyle(
                "-fx-background-color: " + CARD + "; -fx-padding: 0;");
    }

    private void buildContent() {
        VBox container = new VBox(0);
        container.setStyle("-fx-background-color: " + CARD + ";");

        // ── Title area ────────────────────────────────────────────────
        VBox titleBox = new VBox(8);
        titleBox.setAlignment(Pos.CENTER);
        titleBox.setPadding(new Insets(32, 30, 20, 30));
        titleBox.setStyle("-fx-background-color: " + CARD + ";");

        Text title = new Text("Masuk");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));
        title.setFill(Color.WHITE);

        titleBox.getChildren().add(title);

        // ── Form body ─────────────────────────────────────────────────
        VBox body = new VBox(16);
        body.setPadding(new Insets(10, 30, 24, 30));
        body.setStyle("-fx-background-color: " + CARD + ";");

        // User Type
        VBox roleGroup = fieldGroup("User Type :");
        roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll("Majelis", "Admin");
        roleCombo.setValue("Majelis");
        roleCombo.setMaxWidth(Double.MAX_VALUE);
        styleCombo(roleCombo);
        roleGroup.getChildren().add(roleCombo);

        // Username
        VBox userGroup = fieldGroup("Username");
        usernameField = new TextField();
        usernameField.setPromptText("username");
        styleField(usernameField);
        userGroup.getChildren().add(usernameField);

        // Password
        VBox passGroup = fieldGroup("Password");
        passwordField = new PasswordField();
        passwordField.setPromptText("password");
        styleField(passwordField);
        passGroup.getChildren().add(passwordField);

        // Lanjutkan button (inside body so it's full width and styled properly)
        Button btnLanjutkan = new Button("Lanjutkan");
        btnLanjutkan.setMaxWidth(Double.MAX_VALUE);
        btnLanjutkan.setPadding(new Insets(12, 0, 12, 0));
        btnLanjutkan.setFont(Font.font("System", FontWeight.BOLD, 14));
        btnLanjutkan.setStyle(
                "-fx-background-color: " + ACCENT + "; -fx-text-fill: white; " +
                "-fx-background-radius: 22; -fx-cursor: hand; -fx-border-width: 0;");
        btnLanjutkan.setOnMouseEntered(e -> btnLanjutkan.setStyle(
                "-fx-background-color: #3D6FD4; -fx-text-fill: white; " +
                "-fx-background-radius: 22; -fx-cursor: hand; -fx-border-width: 0;"));
        btnLanjutkan.setOnMouseExited(e -> btnLanjutkan.setStyle(
                "-fx-background-color: " + ACCENT + "; -fx-text-fill: white; " +
                "-fx-background-radius: 22; -fx-cursor: hand; -fx-border-width: 0;"));

        body.getChildren().addAll(roleGroup, userGroup, passGroup, btnLanjutkan);

        container.getChildren().addAll(titleBox, body);
        getDialogPane().setContent(container);
        getDialogPane().setPrefWidth(440);

        // ── Buttons (hidden, triggered by custom button) ──────────────
        ButtonType loginBtn  = new ButtonType("OK",     ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelBtn = new ButtonType("Batal",  ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().addAll(loginBtn, cancelBtn);

        Platform.runLater(() -> {
            Node okNode = getDialogPane().lookupButton(loginBtn);
            if (okNode != null) okNode.setVisible(false);
            Node cancelNode = getDialogPane().lookupButton(cancelBtn);
            if (cancelNode != null) cancelNode.setVisible(false);

            // Wire custom button to fire OK
            btnLanjutkan.setOnAction(e -> {
                Node ok = getDialogPane().lookupButton(loginBtn);
                if (ok != null) ok.fireEvent(new ActionEvent());
            });
        });

        passwordField.setOnAction(e -> {
            Node ok = getDialogPane().lookupButton(loginBtn);
            if (ok != null) ok.fireEvent(new ActionEvent());
        });
    }

    private VBox fieldGroup(String labelText) {
        VBox group = new VBox(6);
        Label lbl = new Label(labelText);
        lbl.setFont(Font.font("System", 12));
        lbl.setTextFill(Color.web("rgba(255,255,255,0.85)"));
        group.getChildren().add(lbl);
        return group;
    }

    private void styleField(TextField field) {
        String base = "-fx-background-color: #5C5C5C; " +
                "-fx-border-width: 0; -fx-background-radius: 20; " +
                "-fx-padding: 10 16 10 16; -fx-font-size: 13; -fx-text-fill: white; " +
                "-fx-prompt-text-fill: rgba(255,255,255,0.45);";
        String focused = "-fx-background-color: #686868; " +
                "-fx-border-width: 0; -fx-background-radius: 20; " +
                "-fx-padding: 10 16 10 16; -fx-font-size: 13; -fx-text-fill: white; " +
                "-fx-prompt-text-fill: rgba(255,255,255,0.45);";
        field.setStyle(base);
        field.focusedProperty().addListener((obs, old, focused2) ->
                field.setStyle(focused2 ? focused : base));
    }

    private void styleCombo(ComboBox<?> combo) {
        combo.setStyle(
                "-fx-background-color: #5C5C5C; -fx-border-width: 0; " +
                "-fx-background-radius: 20; -fx-font-size: 13; " +
                "-fx-text-fill: white; -fx-padding: 4 0 4 8;");
    }

    private void setupResultConverter() {
        setResultConverter(btn -> {
            if (btn != null && btn.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                return new String[]{
                    usernameField.getText().trim(),
                    passwordField.getText().trim(),
                    roleCombo.getValue().toLowerCase()
                };
            }
            return null;
        });
    }
}
