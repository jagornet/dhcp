echo on
if "%OS%"=="Windows_NT" @setlocal

rem %~dp0 is expanded pathname of the current script under NT
set DEFAULT_JAGORNET_DHCP_HOME=%~dp0..

if "%JAGORNET_DHCP_HOME%"=="" set JAGORNET_DHCP_HOME=%DEFAULT_JAGORNET_DHCP_HOME%
set DEFAULT_JAGORNET_DHCP_HOME=

:checkJava
set _JAVACMD=%JAVACMD%

if "%JAVA_HOME%" == "" goto noJavaHome
if not exist "%JAVA_HOME%\bin\java.exe" goto noJavaHome
if "%_JAVACMD%" == "" set _JAVACMD=%JAVA_HOME%\bin\java.exe
goto runApp

:noJavaHome
if "%_JAVACMD%" == "" set _JAVACMD=java.exe
echo.
echo Warning: JAVA_HOME environment variable is not set.
echo.

:runApp
"%_JAVACMD%" -cp "%JAGORNET_DHCP_HOME%\conf";"%JAGORNET_DHCP_HOME%\lib\*" -Djagornet.dhcp.home="%JAGORNET_DHCP_HOME%" com.jagornet.dhcp.server.JagornetDhcpServer %*
