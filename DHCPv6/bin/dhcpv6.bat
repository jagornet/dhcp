echo off
if "%OS%"=="Windows_NT" @setlocal

rem %~dp0 is expanded pathname of the current script under NT
set DEFAULT_DHCPV6_HOME=%~dp0..

if "%DHCPV6_HOME%"=="" set DHCPV6_HOME=%DEFAULT_DHCPV6_HOME%
set DEFAULT_DHCPV6_HOME=

:checkJava
set _JAVACMD=%JAVACMD%

if "%JAVA_HOME%" == "" goto noJavaHome
if not exist "%JAVA_HOME%\bin\java.exe" goto noJavaHome
if "%_JAVACMD%" == "" set _JAVACMD=%JAVA_HOME%\bin\java.exe
goto runAnt

:noJavaHome
if "%_JAVACMD%" == "" set _JAVACMD=java.exe
echo.
echo Warning: JAVA_HOME environment variable is not set.
echo.

"%_JAVACMD%" -cp "%DHCPV6_HOME%\conf";"%DHCPV6_HOME%\lib\*" ^
-Ddhcpv6.home="%DHCPV6_HOME%" com.jagornet.dhcpv6.server.DhcpV6Server %*
