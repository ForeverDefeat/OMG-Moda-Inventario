@echo off
setlocal

set "ROOT_DIR=%~dp0"
set "BACKEND_DIR=%ROOT_DIR%sistema-inventario"
set "FRONTEND_DIR=%ROOT_DIR%omg-moda-front"

if not exist "%BACKEND_DIR%" (
  echo No se encontro la carpeta del backend: %BACKEND_DIR%
  pause
  exit /b 1
)

if not exist "%FRONTEND_DIR%" (
  echo No se encontro la carpeta del frontend: %FRONTEND_DIR%
  pause
  exit /b 1
)

echo Iniciando ClothWise...
echo Backend:  http://localhost:8080
echo Frontend: http://localhost:5173
echo.

start "ClothWise Backend" cmd /k "cd /d ""%BACKEND_DIR%"" && mvnw.cmd spring-boot:run"
start "ClothWise Frontend" cmd /k "cd /d ""%FRONTEND_DIR%"" && if not exist node_modules npm install && npm run dev"

echo Se abrieron dos terminales: backend y frontend.
echo Cierra esas ventanas para detener los servicios.
pause
