@echo off
REM Launch Java backend and React frontend for CoDXPTokenTracker on Windows.
REM Usage: double-click start-windows.bat
cd /d "%~dp0"

cd java
start mvn exec:java

cd ..\frontend
npm install
npm run dev

pause
