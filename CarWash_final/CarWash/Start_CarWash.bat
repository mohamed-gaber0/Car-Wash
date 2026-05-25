@echo off
cd /d "%~dp0"
title Car Wash ERP System
color 0B

echo Terminating background Java processes to free up ports...
taskkill /F /IM java.exe >nul 2>&1
taskkill /F /IM javaw.exe >nul 2>&1

echo Starting the system... Please wait.
java --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/java.lang.reflect=ALL-UNNAMED --add-opens=javafx.base/com.sun.javafx.event=ALL-UNNAMED --add-opens=javafx.graphics/com.sun.javafx.application=ALL-UNNAMED --add-opens=javafx.graphics/com.sun.glass.ui=ALL-UNNAMED -jar target\carwash-billing-1.1-SNAPSHOT.jar

echo.
echo ===================================================
echo [INFO] The system closed or an error occurred.
echo ===================================================
pause
