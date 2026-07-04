@echo off
cd /d "%~dp0"
echo Starting Recka...
echo.
echo This launcher uses Maven so JavaFX is loaded automatically.
echo Do NOT start target\recka-1.0.0.jar directly.
echo.
mvn clean javafx:run
if errorlevel 1 (
  echo.
  echo Recka did not start.
  echo Check that Java 21, Maven and MySQL are installed and that application.properties is correct.
  pause
)
