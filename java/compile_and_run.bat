@echo off
setlocal enabledelayedexpansion
title Java Module - Online Course Registration System
cls
REM ==============================================================================
REM Scenario 17: Online Course Registration and Student Enrollment Analysis System
REM Java Compilation and Execution Script
REM ==============================================================================

echo ==============================================================================
echo    ONLINE COURSE REGISTRATION SYSTEM - JAVA APPLICATION LAUNCHER
echo ==============================================================================
echo.

REM 1. Check Java Environment
set "JAVA_CMD="
set "JAVAC_CMD="

where javac >nul 2>nul
if %ERRORLEVEL% EQU 0 (
    set "JAVAC_CMD=javac"
    set "JAVA_CMD=java"
) else (
    for /d %%D in ("C:\Program Files\Microsoft\jdk*" "C:\Program Files\Java\jdk*" "C:\Program Files\Eclipse Adoptium\jdk*" "C:\Program Files\Amazon Corretto\jdk*" "C:\Program Files\Zulu\zulu*") do (
        if exist "%%D\bin\javac.exe" (
            set "JAVAC_CMD=%%D\bin\javac.exe"
            set "JAVA_CMD=%%D\bin\java.exe"
        )
    )
)

if "%JAVA_CMD%"=="" (
    where java >nul 2>nul
    if %ERRORLEVEL% EQU 0 (
        set "JAVA_CMD=java"
    )
)

if "%JAVA_CMD%"=="" (
    echo [X] Java Runtime not found. Please ensure JDK 17+ or 21+ is installed.
    pause
    exit /b 1
)

echo [✓] Java Runtime detected: %JAVA_CMD%

REM 2. Compile all source files
cd /d "%~dp0"
if not exist "bin" mkdir bin

if not "%JAVAC_CMD%"=="" (
    echo [*] Compiling all Java source files...
    "%JAVAC_CMD%" -d bin -cp ".;lib/*" src/*.java
    if !ERRORLEVEL! NEQ 0 (
        echo [X] Compilation failed. Please check errors above.
        pause
        exit /b 1
    )
    echo [✓] Compilation successful! All .class files updated in bin/
) else (
    echo [*] Note: javac compiler not in PATH, attempting execution using compiled bytecode...
)

echo.
echo Select Execution Mode:
echo   [1] Interactive Console Application (Main Menu with 7 Functional Modules)
echo   [2] Automated System Test Suite     (14 Core Verification & Validation Tests)
echo   [3] DBMS Terminal Output Runner     (Full Relational Schema, Tables & Views)
echo   [4] Exit
echo.
set /p user_choice="Enter choice (1-4) [Default: 1]: "

if "%user_choice%"=="" set user_choice=1
if "%user_choice%"=="1" (
    cls
    "%JAVA_CMD%" -cp "bin;lib/*" src.Main
) else if "%user_choice%"=="2" (
    cls
    "%JAVA_CMD%" -cp "bin;lib/*" src.SystemTester
) else if "%user_choice%"=="3" (
    cls
    "%JAVA_CMD%" -cp "bin;lib/*" src.DBMSOutput
) else (
    echo Exiting.
)

pause
