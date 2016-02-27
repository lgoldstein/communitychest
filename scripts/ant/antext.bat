@echo off
@setlocal ENABLEDELAYEDEXPANSION
@setlocal ENABLEEXTENSIONS

rem enables specifiying another JVM rather than the default via -vm ...location...
if "%1" == "-vm" goto setVmArg
   call :setJavaHome "%JAVA_HOME%"

rem enables specifiying another ANT_HOME rather than the default via -anthome ...location...
:initAntHome
if "%1" == "-anthome" goto setAntHomeArg
	call :setAntHome "%ANT_HOME%"

:initCmdLine
set CMD_LINE_ARGS=
:slurpArgs
if {%1} == {} goto doneSlurpArgs
   set CMD_LINE_ARGS=%CMD_LINE_ARGS% %~1
   shift
   goto slurpArgs

:doneSlurpArgs
call "%ANT_HOME%\bin\ant.bat" %CMD_LINE_ARGS%

:end
@endlocal
goto :EOF

:setVmArg
if {%2} == {} goto noArgOption
   shift
   call :setJavaHome %1
   shift
   goto initAntHome

:setAntHomeArg
if {%2} == {} goto noArgOption
   shift
   call :setAntHome %1
   shift
   goto initCmdLine

:noArgOption
   echo Missing %1 option argument
   goto end

:setJavaHome
set JAVA_HOME=%~1
goto :EOF

:setAntHome
set ANT_HOME=%~1
goto :EOF