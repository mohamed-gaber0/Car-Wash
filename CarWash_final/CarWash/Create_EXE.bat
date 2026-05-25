@echo off
title Build CarWash EXE
color 0B

echo =========================================================
echo Starting the build process to generate CarWash.exe...
echo Please wait, this might take a minute or two.
echo =========================================================

powershell.exe -ExecutionPolicy Bypass -File "%~dp0Build_System.ps1"

echo.
pause
