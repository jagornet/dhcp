@echo off
setlocal

rem
rem Find the application home.
rem
rem %~dp0 is location of current script under NT
set JAGORNET_BINPATH=%~dp0

set YAJSW_HOME=%JAGORNET_BINPATH%yajsw-beta-10.2

cd %YAJSW_HOME%\bat

runConsole.bat
