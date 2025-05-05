@echo off
echo Mapping network drive...

:: Clear any existing connections
net use Z: /delete >nul 2>&1

:: Get computer name
for /f "tokens=2 delims=:" %%a in ('ipconfig ^| findstr "IPv4"') do set IP=%%a
set IP=%IP:~1%

echo.
echo Connecting to: \\%IP%\CloudStorage
echo.
echo Note: Use your Windows username and password
echo       (the same credentials you use to log into your computer)
echo.

:: Prompt for credentials
set /p username=Enter your Windows username: 
set /p password=Enter your Windows password: 

:: Map the drive to Z: with credentials
echo.
echo Attempting to connect...
net use Z: \\%IP%\CloudStorage /user:%username% %password% /persistent:yes

if %ERRORLEVEL% EQU 0 (
    echo.
    echo Network drive mapped successfully!
    echo You can now access your files from Z: drive
    echo.
    echo To access your files:
    echo 1. Open File Explorer
    echo 2. Look for Z: drive
    echo 3. Double-click to open
) else (
    echo.
    echo Failed to map network drive.
    echo.
    echo Possible reasons:
    echo 1. Incorrect username or password
    echo 2. Your account is not in the CloudUsers group
    echo 3. The network share is not accessible
    echo.
    echo Please check:
    echo - Are you using your Windows username and password?
    echo - Has your account been added to the CloudUsers group?
    echo - Is the computer with the F: drive turned on?
    echo.
    echo Contact your administrator if the problem persists.
)

echo.
echo Press any key to continue...
pause > nul 