#!/bin/bash
echo "============================================"
echo "   ChatAja - Chatbot Informasi Gereja"
echo "============================================"
echo ""

# Cek Maven
if ! command -v mvn &>/dev/null; then
    echo "[ERROR] Maven tidak ditemukan."
    echo "Install: sudo apt install maven  (Linux)"
    echo "         brew install maven      (macOS)"
    exit 1
fi

# Cek Java
if ! command -v java &>/dev/null; then
    echo "[ERROR] Java tidak ditemukan. Install JDK 17+."
    exit 1
fi

echo "[INFO] Java version: $(java -version 2>&1 | head -1)"
echo "[INFO] Maven version: $(mvn -version 2>&1 | head -1)"
echo ""
echo "[INFO] Menjalankan ChatAja..."
mvn -q clean javafx:run
