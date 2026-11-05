# PowerShell Run Script for Bus & Train Ticket Reservation System
Write-Host "===================================================================" -ForegroundColor Cyan
Write-Host "  Launching Bus/Train Ticket Reservation System (MySQL Mode)..." -ForegroundColor Cyan
Write-Host "===================================================================" -ForegroundColor Cyan

java -cp "bin;lib/*" com.ticketreservation.Main
