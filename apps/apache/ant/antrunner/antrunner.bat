@echo off
@setlocal ENABLEDELAYEDEXPANSION

rem Application startup script $Rev: 568 $
if "%1" == "-vm" goto setVmArg
   call :setJavaHome "%JAVA_HOME%"

:checkJavaExec
set JAVA="%JAVA_HOME%\bin\javaw.exe"
if exist %JAVA% goto checkToolsJar
   echo Missing %JAVA% application
   goto end

:checkToolsJar
if exist "%JAVA_HOME%\lib\tools.jar" goto initAntHome
   echo Missing %JAVA_HOME%\lib\tools.jar - make sure JAVA_HOME points to the JDK
   goto end

:initAntHome
if "%1" == "-anthome" goto setAntHomeArg
	call :setAntHome "%ANT_HOME%"

:checkAntHome
if exist "%ANT_HOME%" goto setClassPath
   echo Missing %ANT_HOME% directory
   goto end

:setClassPath
set MYDIR=%~dp0

rem Add tools.jar to classpath
set CLASSPATH="%JAVA_HOME%\lib\tools.jar"

rem accumulate any JAR found in %ANT_HOME% into the invocation classpath
for %%a in ("%ANT_HOME%\lib\*.jar") do set CLASSPATH=!CLASSPATH!;"%%a"

rem accumulate any JAR found in "lib"  or "bin" sub-folder(s)
for %%l in ("%MYDIR%\lib\*.jar") do set CLASSPATH=!CLASSPATH!;"%%l"
for %%b in ("%MYDIR%\bin\*.jar") do set CLASSPATH=!CLASSPATH!;"%%b"

rem slurp the command line arguments. This loop allows for an unlimited number
rem of arguments (up to the command line limit, anyway).
set ANT_CMD_LINE_ARGS=
:slurpArgs
if {%1} == {} goto doneStart
   set ANT_CMD_LINE_ARGS=%ANT_CMD_LINE_ARGS% %~1
   shift
   goto slurpArgs

:doneStart
rem invoke the ANT builder GUI + include the CTI2 ant task(s) JAR
start "antrunner" /I /B %JAVA% "-Dant.home=%ANT_HOME%" -classpath %CLASSPATH% net.community.apps.apache.ant.antrunner.Main  %ANT_CMD_LINE_ARGS%

:end
@endlocal
goto :EOF

:setVmArg
if {%2} == {} goto noArgOption
   shift
   call :setJavaHome %1
   shift
   goto checkJavaExec

:setAntHomeArg
if {%2} == {} goto noArgOption
   shift
   call :setAntHome %1
   shift
   goto checkAntHome

:noArgOption
   echo Missing %1 option argument
   goto end

:setJavaHome
set JAVA_HOME=%~1
goto :EOF

:setAntHome
set ANT_HOME=%~1
goto :EOF