# PowerShell Compile Script
Write-Host "Compiling Java sources..." -ForegroundColor Cyan
if (!(Test-Path "bin")) { New-Item -ItemType Directory -Path "bin" | Out-Null }
javac -encoding UTF-8 -cp "lib/*" -d bin (Get-ChildItem -Path src -Filter *.java -Recurse | Select-Object -ExpandProperty FullName)
if ($LASTEXITCODE -eq 0) {
    Write-Host "[SUCCESS] Compilation successful!" -ForegroundColor Green
} else {
    Write-Host "[ERROR] Compilation failed!" -ForegroundColor Red
}
