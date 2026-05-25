$ErrorActionPreference = "Stop"
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "Installing Java 17 (Eclipse Temurin JDK) on your machine..." -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan

winget install EclipseAdoptium.Temurin.17.JDK --accept-package-agreements --accept-source-agreements

if ($LASTEXITCODE -eq 0) {
    Write-Host "==========================================" -ForegroundColor Green
    Write-Host "Java 17 installed successfully and added to PATH!" -ForegroundColor Green
    Write-Host "IMPORTANT: Please close VS Code completely and reopen it." -ForegroundColor Green
    Write-Host "After that, Start_CarWash.bat will work perfectly!" -ForegroundColor Green
    Write-Host "==========================================" -ForegroundColor Green
} else {
    Write-Host "An error occurred during installation." -ForegroundColor Red
}

Read-Host "Press Enter to close..."
