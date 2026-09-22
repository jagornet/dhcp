@echo off
if "%OS%"=="Windows_NT" @setlocal

rem Check for help or empty arguments (< 5ms response, no JVM boot required)
if "%~1"=="" goto showHelp
if "%~1"=="-?" goto showHelp
if "%~1"=="/?" goto showHelp
if "%~1"=="-h" goto showHelp
if "%~1"=="--help" goto showHelp
if "%~1"=="help" goto showHelp
goto setupEnv

:showHelp
echo Jagornet DHCP Server
echo Usage: dhcpserver.bat [options]
echo.
echo Server Options:
echo   -c,  --configfile ^<filename^>    Configuration file (default = config\dhcpserver.xml)
echo   -4b, --v4bcast ^<interface^>     DHCPv4 broadcast interface (default = none)
echo   -4u, --v4ucast ^<addresses...^>  DHCPv4 unicast addresses (default = all IPv4 addresses)
echo   -4p, --v4port ^<portnum^>        DHCPv4 port number (default = 67)
echo   -6m, --v6mcast ^<interfaces...^> DHCPv6 multicast interfaces (default = none)
echo   -6u, --v6ucast ^<addresses...^>  DHCPv6 unicast addresses (default = all IPv6 addresses)
echo   -6p, --v6port ^<portnum^>        DHCPv6 port number (default = 547)
echo   -ga, --gaddr ^<address^>         gRPC address (default = all IP addresses, or 'none')
echo   -gp, --gport ^<portnum^>         gRPC port number (default = 9066)
echo   -ha, --haddr ^<address^>         HTTPS address (default = all IP addresses, or 'none')
echo   -hp, --hport ^<portnum^>         HTTPS port number (default = 9067)
echo.
echo Utility Options:
echo   -tc, --test-configfile ^<file^>   Test configuration file syntax, then exit
echo   -li, --list-interfaces          Show detailed host interface list, then exit
echo   -v,  --version                  Show version information, then exit
echo   -?,  --help                     Show this help page
echo.
echo Windows Service Management (WinSW):
echo   jagornet-service.exe install    Install service
echo   jagornet-service.exe start      Start service
echo   jagornet-service.exe stop       Stop service
echo   jagornet-service.exe status     Check service status
goto end

:setupEnv
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
rem Handle start or run verb prefix
set CMD_ARGS=%*
if "%~1"=="start" (
    shift
    set CMD_ARGS=%2 %3 %4 %5 %6 %7 %8 %9
)
if "%~1"=="run" (
    shift
    set CMD_ARGS=%2 %3 %4 %5 %6 %7 %8 %9
)

"%_JAVACMD%" -cp "%JAGORNET_DHCP_HOME%\config";"%JAGORNET_DHCP_HOME%\lib\*" -Djagornet.dhcp.home="%JAGORNET_DHCP_HOME%" com.jagornet.dhcp.server.JagornetDhcpServer %CMD_ARGS%

:end
