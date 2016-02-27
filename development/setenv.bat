rem Edit this file to CATALINA_BASE/bin/setenv.bat to set custom options
rem Tomcat accepts two parameters JAVA_OPTS and CATALINA_OPTS
rem JAVA_OPTS are used during START/STOP/RUN
rem CATALINA_OPTS are used during START/RUN
rem For ERS layout, set CATALINA_BASE here as well

rem Must contain all JVM Options.  Used by AMS.

set GENERAL_JVM_OPTS="-Xmx512m"

rem JVM Sun specific settings
rem For a complete list http://blogs.sun.com/watt/resource/jvm-options-list.html
set SUN_JVM_OPTS="-XX:MaxPermSize=276m"

set "JVM_OPTS=%GENERAL_JVM_OPTS% %SUN_JVM_OPTS%"
set "CATALINA_OPTS=%JVM_OPTS% %APPLICATION_OPTS% -server -Djava.awt.headless=true -Dgemfire.disableShutdownHook=true"

rem set JAVA_HOME=setme
rem set JRE_HOME=setme

if "%CATALINA_BASE%" == "" goto noCatalinaBase
set "CBASE=%CATALINA_BASE%"
goto setClassPath

:noCatalinaBase
set "CBASE=%CATALINA_HOME%"

:setClassPath
set "CATALINA_OPTS=%CATALINA_OPTS% -Dinsight.base=%CBASE%\insight -Dinsight.logs=%CBASE%\logs"
set "CLASSPATH=%CLASSPATH%;%CBASE%\bin\@ASPECTJWEAVER@;%CBASE\lib\@TCSERVERBOOTSTRAP@"