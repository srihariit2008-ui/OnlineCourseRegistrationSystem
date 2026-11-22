@echo off
REM ==============================================================================
REM Online Course Registration System - DBMS Terminal Output Runner
REM Runs directly in Windows Command Prompt (CMD) or PowerShell
REM ==============================================================================
cd /d "%~dp0java"
echo [*] Compiling and Executing DBMS Output Runner...
javac -d bin -cp ".;lib/*" src/DBMSOutput.java
java -cp "bin;lib/*" src.DBMSOutput
pause
