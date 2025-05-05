@echo off
echo Setting up network share...

:: Check if running as administrator
net session >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo This script must be run as Administrator.
    echo Please right-click and select "Run as administrator"
    pause
    exit /b 1
)

:: Check if F: drive exists
if not exist F:\ (
    echo Error: F: drive not found.
    echo Please ensure the F: drive is connected and accessible.
    pause
    exit /b 1
)

:: Create the cloud storage directory if it doesn't exist
if not exist "F:\cloud_storage" (
    echo Creating cloud storage directory...
    mkdir "F:\cloud_storage"
    if %ERRORLEVEL% NEQ 0 (
        echo Error: Failed to create directory.
        pause
        exit /b 1
    )
)

:: Create a security group for cloud storage access
echo Creating CloudUsers group...
net localgroup CloudUsers /add >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo Warning: CloudUsers group may already exist.
)

:: Add current user to the group
echo Adding current user to CloudUsers group...
net localgroup CloudUsers %USERNAME% /add >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo Warning: User may already be in the group.
)

:: Share the directory with specific permissions
echo Setting up network share...
net share CloudStorage="F:\cloud_storage" /GRANT:CloudUsers,FULL
if %ERRORLEVEL% NEQ 0 (
    echo Error: Failed to create network share.
    pause
    exit /b 1
)

:: Set secure permissions
echo Setting permissions...
icacls "F:\cloud_storage" /inheritance:r
icacls "F:\cloud_storage" /grant:r "CloudUsers:(OI)(CI)F"
icacls "F:\cloud_storage" /grant:r "Administrators:(OI)(CI)F"
icacls "F:\cloud_storage" /grant:r "SYSTEM:(OI)(CI)F"

echo.
echo Network share setup complete!
echo.
echo Security settings:
echo - Created CloudUsers group
echo - Added current user to CloudUsers group
echo - Only CloudUsers group members can access the share
echo - Administrators and SYSTEM have full access
echo.
echo To add more users, run:
echo net localgroup CloudUsers USERNAME /add
echo.
echo You can access your files using:
echo \\192.168.8.15\CloudStorage
echo or
echo \\%COMPUTERNAME%\CloudStorage
echo.
echo Press any key to continue...
pause > nul 