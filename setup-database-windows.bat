@echo off
cd /d "%~dp0"
echo Recka database setup
echo.
echo WARNING: Do not run this script against an existing database that already contains important data.
echo Use it only for a completely empty/new local setup.
echo.
echo This script requires mysql.exe in PATH.
echo If mysql command is missing, open MySQL Workbench or MySQL Installer and make sure MySQL Server/Command Line Client is installed.
echo.
set /p MYSQL_USER=MySQL username [root]: 
if "%MYSQL_USER%"=="" set MYSQL_USER=root
mysql -u %MYSQL_USER% -p < database\schema.sql
if errorlevel 1 goto error
mysql -u %MYSQL_USER% -p < database\seed.sql
if errorlevel 1 goto error
echo.
echo Database setup complete.
pause
exit /b 0
:error
echo.
echo Database setup failed. Check that MySQL Server is running and username/password are correct.
pause
exit /b 1
