@echo off
REM ------------------------------------------------------------------------
REM Licensed to the Apache Software Foundation (ASF) under one or more
REM contributor license agreements.  See the NOTICE file distributed with
REM this work for additional information regarding copyright ownership.
REM The ASF licenses this file to You under the Apache License, Version 2.0
REM (the "License"); you may not use this file except in compliance with
REM the License.  You may obtain a copy of the License at
REM 
REM http://www.apache.org/licenses/LICENSE-2.0
REM 
REM Unless required by applicable law or agreed to in writing, software
REM distributed under the License is distributed on an "AS IS" BASIS,
REM WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
REM See the License for the specific language governing permissions and
REM limitations under the License.
REM ------------------------------------------------------------------------

if "%OS%"=="Windows_NT" @setlocal

rem %~dp0 is expanded pathname of the current script under NT
set DEFAULT_DHCPV6_HOME=%~dp0..

if "%DHCPV6_HOME%"=="" set DHCPV6_HOME=%DEFAULT_DHCPV6_HOME%
set DEFAULT_DHCPV6_HOME=

:doneStart
REM AGR - rem find DHCPV6_HOME if it does not exist due to either an invalid value passed
REM AGR - rem by the user or the %0 problem on Windows 9x
REM AGR - if exist "%DHCPV6_HOME%\README.txt" goto checkJava
REM AGR - 
REM AGR - rem check for dhcpv6 in Program Files on system drive
REM AGR - if not exist "%SystemDrive%\Program Files\dhcpv6" goto checkSystemDrive
REM AGR - set DHCPV6_HOME=%SystemDrive%\Program Files\dhcpv6
REM AGR - goto checkJava
REM AGR - 
REM AGR - :checkSystemDrive
REM AGR - rem check for dhcpv6 in root directory of system drive
REM AGR - if not exist %SystemDrive%\dhcpv6\README.txt goto checkCDrive
REM AGR - set DHCPV6_HOME=%SystemDrive%\dhcpv6
REM AGR - goto checkJava
REM AGR - 
REM AGR - :checkCDrive
REM AGR - rem check for dhcpv6 in C:\dhcpv6 for Win9X users
REM AGR - if not exist C:\dhcpv6\README.txt goto noDhcpv6Home
REM AGR - set DHCPV6_HOME=C:\dhcpv6
REM AGR - goto checkJava
REM AGR - 
REM AGR - :noDhcpv6Home
REM AGR - echo DHCPV6_HOME is set incorrectly or dhcpv6 could not be located. Please set DHCPV6_HOME.
REM AGR - goto end

:checkJava
set _JAVACMD=%JAVACMD%

if "%JAVA_HOME%" == "" goto noJavaHome
if not exist "%JAVA_HOME%\bin\java.exe" goto noJavaHome
if "%_JAVACMD%" == "" set _JAVACMD=%JAVA_HOME%\bin\java.exe
goto runDhcpv6

:noJavaHome
if "%_JAVACMD%" == "" set _JAVACMD=java.exe
echo.
echo Warning: JAVA_HOME environment variable is not set.
echo.

:runDhcpv6

REM AGR - 
REM AGR - if "%DHCPV6_OPTS%" == "" set DHCPV6_OPTS=-Xmx512M
REM AGR - 
REM AGR - if "%SUNJMX%" == "" set SUNJMX=-Dcom.sun.management.jmxremote
REM AGR - REM set SUNJMX=-Dcom.sun.management.jmxremote.port=1099 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false
REM AGR - 
REM AGR - if "%SSL_OPTS%" == "" set SSL_OPTS=-Djavax.net.ssl.keyStorePassword=password -Djavax.net.ssl.trustStorePassword=password -Djavax.net.ssl.keyStore="%DHCPV6_HOME%/conf/broker.ks" -Djavax.net.ssl.trustStore="%DHCPV6_HOME%/conf/broker.ts"
REM AGR - 
REM AGR - REM Uncomment to enable YourKit profiling
REM AGR - REM SET DHCPV6_DEBUG_OPTS="-agentlib:yjpagent"
REM AGR - 
REM AGR - REM Uncomment to enable remote debugging
REM AGR - REM SET DHCPV6_DEBUG_OPTS=-Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005
REM AGR - 

SET DHCPV6_CLASSPATH=%DHCPV6_HOME%\conf
FOR /R %DHCPV6_HOME%\lib %%f IN (*.jar) DO SET DHCPV6_CLASSPATH=!DHCPV6_CLASSPATH!;%%f
REM echo %DHCPV6_CLASSPATH%

REM AGR - 
REM AGR - "%_JAVACMD%" %SUNJMX% %DHCPV6_DEBUG_OPTS% %DHCPV6_OPTS% %SSL_OPTS% -Ddhcpv6.classpath="%DHCPV6_CLASSPATH%" -Ddhcpv6.home="%DHCPV6_HOME%"

SET DHCPV6_MAIN=com.agr.dhcpv6.server.DhcpV6Server

SET CONFIG_FILE="%DHCPV6_HOME%/conf/dhcpv6server.xml"
SET PORT=547

REM Note: log4j.configuration property references file in classpath
SET LOG4J=-Dlog4j.configuration=log4j.properties
SET JMX=-Dcom.sun.management.jmxremote.port=14700 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false
SET JAVA_OPTS=%LOG4J% %JMX%

echo "Starting DHCPv6 Server..."
java -server %JAVA_OPTS% -Ddhcpv6.home="%DHCPV6_HOME%" -cp "%DHCPV6_CLASSPATH%" %DHCPV6_MAIN% -c "%CONFIG_FILE%" -p %PORT% > "%DHCPV6_HOME%/log/dhcpv6server.out" 

goto end


:end
set _JAVACMD=
if "%OS%"=="Windows_NT" @endlocal
