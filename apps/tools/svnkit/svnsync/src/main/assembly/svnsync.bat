@echo off
@setlocal ENABLEDELAYEDEXPANSION

rem Application startup script $Rev: 586 $
if "%1" == "-vm" goto setVmArg
   call :setJavaHome "%JAVA_HOME%"

:checkJavaExec
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
set SVNSYNC_CMD_LINE_ARG=
:slurpArgs
if {%1} == {} goto doneStart
   set SVNSYNC_CMD_LINE_ARG=%SVNSYNC_CMD_LINE_ARG% %~1
   shift
   goto slurpArgs

:doneStart
rem invoke the GUI
start "svnsync" /I /B %JAVA% -classpath %CLASSPATH% net.community.apps.tools.svn.svnsync.Main  %SVNSYNC_CMD_LINE_ARG%

:end
@endlocal
goto :EOF

:noArgOption
   echo Missing %1 option argument
   goto end

:setJavaHome
set JAVA_HOME=%~1
goto :EOF

:setAntHome
set ANT_HOME=%~1
goto :EOF