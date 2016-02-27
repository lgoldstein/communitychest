@echo off
mvn -s "%CD%\settings.xml" -Dmaven.test.skip=true install
pause