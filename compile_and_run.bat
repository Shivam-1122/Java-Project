@echo off
call compile.bat
if %ERRORLEVEL% EQU 0 (
    call run.bat
)
