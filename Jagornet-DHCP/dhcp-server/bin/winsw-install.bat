@echo off
setlocal
set BIN_DIR=%~dp0
cd /d "%BIN_DIR%"

if not exist "%BIN_DIR%jagornet-service.exe" (
    echo.
    echo ======================================================================
    echo WinSW executable not found: %BIN_DIR%jagornet-service.exe
    echo.
    echo To install Jagornet DHCP Server as a Windows Service using WinSW:
    echo 1. Download WinSW-x64.exe from:
    echo    https://github.com/winsw/winsw/releases
    echo 2. Copy/rename it to:
    echo    %BIN_DIR%jagornet-service.exe
    echo 3. Run this script again as Administrator.
    echo ======================================================================
    echo.
    pause
    exit /b 1
)

echo Installing Jagornet DHCP Server service...
"%BIN_DIR%jagornet-service.exe" install "%BIN_DIR%jagornet-service.xml"
if %ERRORLEVEL% equ 0 (
    echo.
    echo Starting Jagornet DHCP Server service...
    "%BIN_DIR%jagornet-service.exe" start
)
pause
