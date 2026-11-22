# ==============================================================================
# Online Course Registration System - DBMS Terminal Output Runner (PowerShell)
# Usage: .\run_dbms.ps1
# ==============================================================================
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location (Join-Path $scriptDir "java")
Write-Host "[*] Compiling DBMS Output Runner..." -ForegroundColor Cyan
javac -d bin -cp ".;lib/*" src/DBMSOutput.java
Write-Host "[*] Displaying Relational DBMS Output..." -ForegroundColor Green
java -cp "bin;lib/*" src.DBMSOutput
