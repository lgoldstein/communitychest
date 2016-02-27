@echo off
@setlocal ENABLEDELAYEDEXPANSION

if "%1" == "-vm" goto setVmArg
   call :setJavaHome "%JAVA_HOME%"

:checkJavaExec
call :setJavaHome "%JAVA_HOME%"
set JAVA="%JAVA_HOME%\bin\javaw.exe"
if exist %JAVA% goto setClassPath
   echo Missing %JAVA% application
   goto end

:setClassPath
set MYDIR=%~dp0
set CLASSPATH=
rem accumulate any JAR found in "lib" or "bin" sub-folder(s)
for %%l in ("%MYDIR%\lib\*.jar") do set CLASSPATH=!CLASSPATH!;"%%l"
for %%b in ("%MYDIR%\bin\*.jar") do set CLASSPATH=!CLASSPATH!;"%%b"

rem slurp the command line arguments. This loop allows for an unlimited number
rem of arguments (up to the command line limit, anyway).
set CMD_LINE_ARGS=
:slurpArgs
if {%1} == {} goto doneStart
   set CMD_LINE_ARGS=%CMD_LINE_ARGS% %~1
   shift
   goto slurpArgs

:doneStart

start "jarscanner" /I /B %JAVA% -classpath %CLASSPATH% net.community.apps.tools.jarscanner.Main %CMD_LINE_ARGS%

:end
@endlocal
goto :EOF

:setVmArg
if {%2} == {} goto noArgOption
   shift
   call :setJavaHome %1
   shift
   goto checkJavaExec

:noArgOption
   echo Missing %1 option argument
   goto end

:setJavaHome
set JAVA_HOME=%~1
goto :EOF
