::
:: IT Compliance Analyzer - Storage Edition
:: Databse update script
::

::set SQLANY10="C:\sybase\SQL Anywhere 10"
::set SQLANY10_DATA_DIR=%SQLANYSAMP10%
::set SQLANY10_DUMP=.
:: Name of DB file to create
set ITCASENAME=itcase
set SALANG=EN

call "%SQLANY10%\win32\dbisql" -nogui -c "dbf=%SQLANYSAMP10%\%ITCASENAME%.db;uid=dba;pwd=sql" .\update_load.sql
