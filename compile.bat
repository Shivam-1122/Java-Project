@echo off
echo ===================================================================
echo   Compiling Bus/Train Ticket Reservation System...
echo ===================================================================

if not exist bin mkdir bin

javac -encoding UTF-8 -cp "lib/*" -d bin src/com/ticketreservation/*.java src/com/ticketreservation/model/*.java src/com/ticketreservation/database/*.java src/com/ticketreservation/ai/*.java src/com/ticketreservation/ui/*.java

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Compilation failed.
    pause
    exit /b %ERRORLEVEL%
)

echo [SUCCESS] Compilation finished successfully into bin/ directory!
