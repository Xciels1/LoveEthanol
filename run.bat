@echo off
echo ============================================
echo    ChatAja - Chatbot Informasi Gereja
echo ============================================
echo.

REM Cek Maven tersedia
where mvn >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Maven tidak ditemukan. Pastikan Maven sudah diinstall dan ada di PATH.
    echo Download: https://maven.apache.org/download.cgi
    pause
    exit /b 1
)

REM Cek Java tersedia
where java >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Java tidak ditemukan. Pastikan JDK 17+ sudah diinstall.
    echo Download: https://adoptium.net/
    pause
    exit /b 1
)

echo [INFO] Mengompilasi dan menjalankan ChatAja...
echo.
mvn -q clean javafx:run

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Gagal menjalankan aplikasi. Pastikan semua dependency tersedia.
    echo Coba jalankan: mvn clean package
    pause
)
