$ErrorActionPreference = "Stop"

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "Setting up Maven to build the system..." -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan

$mavenUrl = "https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip"
$mavenZip = "$env:TEMP\maven.zip"
$mavenDir = "$env:TEMP\maven_extracted"

Write-Host "[1/3] Downloading Maven..."
Invoke-WebRequest -Uri $mavenUrl -OutFile $mavenZip

Write-Host "[2/3] Extracting files..."
if (Test-Path $mavenDir) { Remove-Item -Recurse -Force $mavenDir }
Expand-Archive -Path $mavenZip -DestinationPath $mavenDir -Force

$mvnCmd = "$mavenDir\apache-maven-3.9.6\bin\mvn.cmd"

Write-Host "[3/3] Building the JAR file..." -ForegroundColor Yellow
Set-Location -Path $PSScriptRoot
& $mvnCmd package -DskipTests

if ($LASTEXITCODE -eq 0) {
    Write-Host "==========================================" -ForegroundColor Green
    Write-Host "System built successfully! JAR is ready." -ForegroundColor Green
    Write-Host "Generating CarWash.exe..." -ForegroundColor Green
    Copy-Item -Path "target\CarWash.exe" -Destination "CarWash.exe" -Force
    Write-Host "CarWash.exe has been created in the main folder!" -ForegroundColor Green
    Write-Host "You can now run CarWash.exe directly." -ForegroundColor Green
    Write-Host "==========================================" -ForegroundColor Green
} else {
    Write-Host "An error occurred during the build." -ForegroundColor Red
}

Read-Host "Press Enter to close..."
