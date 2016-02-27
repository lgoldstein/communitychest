::
:: IT Compliance Analyzer - Storage Edition
:: Databse install script
::

::set SQLANY10="C:\sybase\SQL Anywhere 10"
::set SQLANY10_DATA_DIR=%SQLANYSAMP10%
::set SQLANY10_DUMP=.
:: Name of DB file to create
set ITCASENAME=itcase
set SALANG=EN

call del /Q /F "%SQLANYSAMP10%\%ITCASENAME%.db" "%SQLANYSAMP10%\%ITCASENAME%.log"

call "%SQLANY10%\win32\dbinit" "%SQLANYSAMP10%\%ITCASENAME%.db"
call "%SQLANY10%\win32\dbisql" -nogui -c "dbf=%SQLANYSAMP10%\%ITCASENAME%.db;uid=dba;pwd=sql" .\load.sql
call "%SQLANY10%\win32\dbisql" -nogui -c "dbf=%SQLANYSAMP10%\%ITCASENAME%.db;uid=dba;pwd=sql" .\dbMaintBackup-sp.sql
call "%SQLANY10%\win32\dbisql" -nogui -c "dbf=%SQLANYSAMP10%\%ITCASENAME%.db;uid=dba;pwd=sql" .\dbMaintRetention-sp.sql
