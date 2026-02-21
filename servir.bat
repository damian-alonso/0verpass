@echo off
cd /d "%~dp0"
echo Sirviendo 0verpass en http://localhost:8080
echo Abriendo el navegador...
start http://localhost:8080
python -m http.server 8080
