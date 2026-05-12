package com.chataja;

import com.chataja.ui.ChatAjaApp;

/**
 * Entry point utama aplikasi ChatAja.
 * Kelas ini diperlukan karena JavaFX membutuhkan
 * kelas launcher terpisah saat dikemas dalam fat JAR.
 */
public class Main {
    public static void main(String[] args) {
        ChatAjaApp.launch(ChatAjaApp.class, args);
    }
}
