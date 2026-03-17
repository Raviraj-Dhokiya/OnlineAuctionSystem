@echo off
color 0A
title Online Auction System - Startup
echo.
echo ========================================================
echo   Setting up Maven and starting the Auction System...
echo ========================================================
echo.

set PATH=%USERPROFILE%\apache-maven-3.9.6\bin;%PATH%

:: ─── DB Credentials — Load from local env.bat (NOT committed to git) ─────────
:: Create a file called env.bat next to startApp.bat with your real credentials.
:: See env.example for the template. env.bat is in .gitignore (safe).
if exist "%~dp0env.bat" (
    echo [INFO] Loading DB credentials from env.bat...
    call "%~dp0env.bat"
) else (
    echo [WARNING] env.bat not found! DB_URL, DB_USER, DB_PASS must be set manually.
    echo           Copy env.example to env.bat and fill in your credentials.
    echo.
)

:: Verify that environment variables are set
if "%DB_URL%"=="" (
    echo [ERROR] DB_URL is not set. Please create env.bat. See env.example for help.
    pause
    exit /b 1
)
if "%DB_USER%"=="" (
    echo [ERROR] DB_USER is not set. Please create env.bat. See env.example for help.
    pause
    exit /b 1
)
if "%DB_PASS%"=="" (
    echo [ERROR] DB_PASS is not set. Please create env.bat. See env.example for help.
    pause
    exit /b 1
)

echo [INFO] DB credentials loaded. Starting application...
echo.

echo Running Maven command: mvn clean compile tomcat7:run
echo.
call mvn clean compile tomcat7:run

echo.
pause
