@echo off
title DBMS Module - Course Registration SQL Query Runner
cls

echo [*] Checking Python Environment for DBMS Runner...

set "PYTHON_CMD="

REM 1. Check LibreOffice Python
if exist "C:\Program Files\LibreOffice\program\python.exe" (
    set "PYTHON_CMD=C:\Program Files\LibreOffice\program\python.exe"
    goto :RUN
)

REM 2. Check PATH python
where python >nul 2>nul
if %ERRORLEVEL% EQU 0 (
    set "PYTHON_CMD=python"
    goto :RUN
)

REM 3. Check py launcher
where py >nul 2>nul
if %ERRORLEVEL% EQU 0 (
    set "PYTHON_CMD=py"
    goto :RUN
)

echo [X] Python was not found on your system.
echo [*] Falling back to Java DBMS Terminal Output Runner...
cd /d "%~dp0..\java"
javac -d bin -cp ".;lib/*" src/DBMSOutput.java
java -cp "bin;lib/*" src.DBMSOutput
pause
exit /b 0

:RUN
echo [✓] Found Python: %PYTHON_CMD%
echo [✓] Launching DBMS Terminal Query Runner...
echo.
"%PYTHON_CMD%" "%~dp0run_dbms.py"
pause
