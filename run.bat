@echo off
echo ===================================================================
echo   Launching Bus/Train Ticket Reservation System...
echo ===================================================================

java -cp "bin;lib/*" com.ticketreservation.Main

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Application exited with code %ERRORLEVEL%.
    pause
)
