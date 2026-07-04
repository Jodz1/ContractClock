@echo off
cd /d "%~dp0"
echo Starting Recka through Maven exec plugin...
echo.
mvn clean compile exec:java
if errorlevel 1 (
  echo.
  echo Recka did not start.
  echo Check that Java 21, Maven and MySQL are installed and that application.properties is correct.
  pause
)
