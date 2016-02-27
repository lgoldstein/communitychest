@echo off
rem see http://www.dostips.com/DtTipsStringManipulation.php#Snippets.SplitString

for /f "tokens=1,2,3,4 delims=/ " %%a in ('date/t') do set WDAY=%%a&set MONTH=%%b&set DAY=%%c&set YEAR=%%d
for /f "tokens=1,2,3 delims=: " %%a in ('time/t') do set HOUR=%%a&set MINUTE=%%b&set AMPM=%%c
echo %YEAR%%MONTH%%DAY%%HOUR%%MINUTE%
