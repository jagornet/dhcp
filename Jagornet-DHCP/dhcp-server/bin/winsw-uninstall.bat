@echo off
setlocal
set BIN_DIR=%~dp0
cd /d "%BIN_DIR%"

if not exist "%BIN_DIR%jagornet-service.exe" (
    echo jagornet-service.exe not found in %BIN_DIR%
    pause
    exit /b 1
)

echo Stopping Jagornet DHCP Server service...
"%BIN_DIR%jagornet-service.exe" stop

echo Uninstalling Jagornet DHCP Server service...
"%BIN_DIR%jagornet-service.exe" uninstall
pause
