@echo off
setlocal enableextensions enabledelayedexpansion

rem ===================================================
rem
rem	Invocation: svcinstall <catalina-home> <catalina-base> <dashboard-ip> <service-name>
rem 
rem ===================================================

if "%~1" == "" (
	set /p CATALINA_HOME="Enter CATALINA_HOME: "
) else (
	set "CATALINA_HOME=%~1"
)
echo Setting CATALINA_HOME to %CATALINA_HOME%

if "!CATALINA_HOME!" == "" (
	echo Must specify a value for CATALINA_HOME
	exit /b 1
	goto end
)

if not exist "!CATALINA_HOME!" (
	echo Specified CATALINA_HOME does not exist or is not a folder: %CATALINA_HOME%
	exit /b 1
	goto end
)

rem find the non 'w' executable
for %%l in ("%CATALINA_HOME%\bin\Tomcat*.exe") do (
	set ORG_CMD=%%l
	set "XLT_CMD=!ORG_CMD:w.exe=!"
	if "!ORG_CMD!" == "!XLT_CMD!" set TOMCAT_CMD=!ORG_CMD!
)

if "!TOMCAT_CMD!" == "" (
	echo Cannot find Tomcat service manipulation application in %CATALINA_HOME%\bin
	exit /b 1
	goto end
)

if "%~2" == "" (
	set /p CATALINA_BASE="Enter CATALINA_BASE (default=%CATALINA_HOME%): "
) else (
	set "CATALINA_BASE=%~2"
)

if "%CATALINA_BASE%" == "" (
	echo Using same value as CATALINA_HOME
	set "CATALINA_BASE=%CATALINA_HOME%"
)

if not exist "!CATALINA_BASE!" (
	echo Specified CATALINA_BASE does not exist or is not a folder: %CATALINA_BASE%
	exit /b 1
	goto end
)

if "%~3" == "" (
	set /p DASHBOARD_IP="Enter dashboard IP: "
) else (
	set "DASHBOARD_IP=%~3"
)

if "%DASHBOARD_IP%" == "" (
	echo Must specify a value for dashboard IP
	exit /b 1
	goto end
)

if "%~4" == "" (
	set /p SVC_NAME="Enter Tomcat service name: "
) else (
	set "SVC_NAME=%~4"
)

if "%SVC_NAME%" == "" (
	echo No Tomcat service name provided
	exit /b 1
	goto end
)

:startInstallation
set "DIST_PATH=%~dp0"
call "%DIST_PATH%\install.bat" "%CATALINA_HOME%" "%CATALINA_BASE%" "%DASHBOARD_IP%"

echo Stopping %SVC_NAME% service
"!TOMCAT_CMD!" //SS//%SVC_NAME%

echo Modifying %SVC_NAME% service configuration via !TOMCAT_CMD!
"!TOMCAT_CMD!" //US//%SVC_NAME% --JvmMx=512 --JvmMs=324 --JvmSs=256
"!TOMCAT_CMD!" //US//%SVC_NAME% ++JvmOptions=-XX:MaxPermSize=276m
"!TOMCAT_CMD!" //US//%SVC_NAME% ++JvmOptions="-Dinsight.base=%CATALINA_HOME%\insight";"-Dinsight.logs=%CATALINA_BASE%\logs"
"!TOMCAT_CMD!" //US//%SVC_NAME% ++JvmOptions="-Djava.library.path=%CATALINA_HOME%\insight\sigar-lib"
"!TOMCAT_CMD!" //US//%SVC_NAME% ++JvmOptions="-javaagent:%CATALINA_HOME%\bin\aspectjweaver-1.6.12.M2.jar";-Daspectj.overweaving=true
rem NOTE: the ++Classpath option does not append but replaces (same as --Classpath) so we re-specify bootstrap and juli
"!TOMCAT_CMD!" //US//%SVC_NAME% ++Classpath="%CATALINA_HOME%\bin\insight-bootstrap-tomcat-extlibs-1.7.0.CI-SNAPSHOT.jar";"%CATALINA_HOME%\bin\insight-annotation-1.7.0.CI-SNAPSHOT.jar";"%CATALINA_HOME%\bin\insight-intercept-1.7.0.CI-SNAPSHOT.jar";"%CATALINA_HOME%\bin\insight-util-1.7.0.CI-SNAPSHOT.jar;"%CATALINA_HOME%\bin\bootstrap.jar";"%CATALINA_HOME%\bin\tomcat-juli.jar"

echo Done - remember to start the service (!)

:end
endlocal
goto :EOF