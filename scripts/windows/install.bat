@echo off
setlocal enableextensions enabledelayedexpansion

rem ====================================================================
rem
rem	Invocation: install <catalina-home> <catalina-base> <dashboard-ip>
rem 
rem ====================================================================

if "%~1" == "" (
	set /p CATALINA_HOME="Enter CATALINA_HOME: "
) else (
	set "CATALINA_HOME=%~1"
)

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

if "%~2" == "" (
	set /p CATALINA_BASE="Enter CATALINA_BASE (default=%CATALINA_HOME%): "
) else (
	set "CATALINA_BASE=%~2"
)

if "!CATALINA_BASE!" == "" (
	echo Using same value as CATALINA_HOME
	set "CATALINA_BASE=!CATALINA_HOME!"
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

echo Setting CATALINA_HOME to %CATALINA_HOME%
echo Setting CATALINA_BASE to %CATALINA_BASE%
echo Dashboard IP is %DASHBOARD_IP%

rem see http://www.dostips.com/DtTipsStringManipulation.php#Snippets.SplitString
for /f "tokens=1,2,3,4 delims=/ " %%a in ('date/t') do set WDAY=%%a&set MONTH=%%b&set DAY=%%c&set YEAR=%%d
for /f "tokens=1,2,3 delims=: " %%a in ('time/t') do set HOUR=%%a&set MINUTE=%%b&set AMPM=%%c
set "TIMESTAMP=%YEAR%-%MONTH%-%DAY%-%HOUR%-%MINUTE%"

rem First backup CATALINA_HOME
call :backupFolder "%CATALINA_HOME%" home

rem check if need to backup CATALINA_BASE as well
if not "%CATALINA_HOME%" == "%CATALINA_BASE%" (
	call :backupFolder "%CATALINA_BASE%" base
)

:startInstallation
set "DIST_PATH=%~dp0"
rem VBScript used for search&replace
set "SRSCRIPT_PATH=%DIST_PATH%SearchAndReplace.vbs"

echo Installing from %DIST_PATH%

echo Copying files from %DIST_PATH%lib to %CATALINA_HOME%\lib
rem see http://www.microsoft.com/resources/documentation/windows/xp/all/proddocs/en-us/copy.mspx for available flags
copy /v /y "%DIST_PATH%lib\*" "%CATALINA_HOME%\lib" >nul

rem delete target insight library if exists
if exist "%CATALINA_HOME%\insight" (
    echo %CATALINA_HOME%\insight exists deleting it
	call :removeFolder "%CATALINA_HOME%\insight"
)

rem copy insight directory
echo Copying insight directory from %DIST_PATH%insight to %CATALINA_HOME%\insight
call :copyFolder "%DIST_PATH%insight" "%CATALINA_HOME%\insight"

rem delete target webapps insight agent folder if exists
if exist "%CATALINA_BASE%\webapps\insight-agent" (
	echo %CATALINA_BASE%\webapps\insight-agent exists, deleting it
	call :removeFolder "%CATALINA_BASE%\webapps\insight-agent"
)

echo Copying insight war from %DIST_PATH%webapps to %CATALINA_BASE%\webapps
copy /v /y "%DIST_PATH%webapps\insight-agent.war" "%CATALINA_BASE%\webapps" >nul

rem copy files from bin
echo Copying aspectjweaver and insight JARs to %CATALINA_HOME%\bin
copy /v /y "%DIST_PATH%bin\aspectjweaver-*.jar" "%CATALINA_HOME%\bin" >nul
copy /v /y "%DIST_PATH%bin\insight-*.jar" "%CATALINA_HOME%\bin" >nul

rem setenv.bat changes
set "RUNSCRIPT=%CATALINA_BASE%\bin\setenv.bat"
if exist "%RUNSCRIPT%" (
	echo %RUNSCRIPT% exists
	for /f "delims=@" %%a in ('findstr /L insight.base "%RUNSCRIPT%"') do set GREP_RESULT=foundIt
) else (
	echo %RUNSCRIPT% does not exist - copying our file
    if not exist "%CATALINA_BASE%\bin" (
		echo %CATALINA_BASE%\bin does not exist, creating it
		mkdir "%CATALINA_BASE%\bin"
	)
	copy /v /y "%DIST_PATH%bin\setenv.bat" "%RUNSCRIPT%" >nul
	set GREP_RESULT=skipIt
)

rem echo GREP_RESULT: %GREP_RESULT%

if not "%GREP_RESULT%" == "skipIt" (
	if "%GREP_RESULT%" == "" (
		echo Concating our changes to the existing file
		type "%DIST_PATH%bin\setenv.bat" >> "%RUNSCRIPT%"
	) else (
		echo Detected insight.base definition, assuming setenv.bat is already modified, doing nothing
	)
)

rem change the insight properties file
set "INSIGHT_PROPS_FILE=%CATALINA_HOME%\insight\insight.properties"
echo Update %INSIGHT_PROPS_FILE%

rem change the IP
cscript //Nologo "%SRSCRIPT_PATH%" "%INSIGHT_PROPS_FILE%" "${dashboard.connect.uri:nio://127.0.0.1:21234}" "nio://%DASHBOARD_IP%:21234"

rem change the auth
cscript //Nologo "%SRSCRIPT_PATH%" "%INSIGHT_PROPS_FILE%" "${agent.auth:agent}" "agent"
cscript //Nologo "%SRSCRIPT_PATH%" "%INSIGHT_PROPS_FILE%" "${agent.auth.password:insight}" "insight"

echo Done.

:end
endlocal
goto :EOF

rem ==================== "Macros" ===========================

rem %1 - from folder, %2 - backup type (home, base)
:backupFolder
	set "BKPSOURCEPATH=%~1"
	rem extract the last path component to use it to build the target path
	set "BKPBASENAME=%~nx$BKPSOURCEPATH:1"
	set "BKPTARGETPATH=%TEMP%\%BKPBASENAME%-%TIMESTAMP%-%~2"

	if exist "%BKPTARGETPATH%" (
		echo Removing backup target folder %BKPTARGETPATH%
		call :removeFolder "%BKPTARGETPATH%"
	)
	
	echo Backing up %BKPSOURCEPATH% to %BKPTARGETPATH%
	call :copyFolder "%BKPSOURCEPATH%" "%BKPTARGETPATH%"
	goto :EOF

rem %1 - folder to be deleted
:removeFolder
	rem see http://www.microsoft.com/resources/documentation/windows/xp/all/proddocs/en-us/rmdir.mspx for available flags
	rmdir /s /q "%~1"
	if not "%ERRORLEVEL%" == "0" (
		echo Failed to remove target: %ERRORLEVEL% - %~1
		exit /b %ERRORLEVEL%
		goto end
	)

	goto :EOF

rem %1 - source, %2 target
:copyFolder
	rem see http://www.microsoft.com/resources/documentation/windows/xp/all/proddocs/en-us/xcopy.mspx for available switches
	xcopy "%~1" "%~2" /i /q /v /s /e /k /x /y
	if not "%ERRORLEVEL%" == "0" (
		echo Failed to backup %~1 to target: %ERRORLEVEL% - %~2
		exit /b %ERRORLEVEL%
		goto end
	)

	goto :EOF
