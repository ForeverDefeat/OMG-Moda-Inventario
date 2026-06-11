#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND_DIR="$ROOT_DIR/sistema-inventario"
FRONTEND_DIR="$ROOT_DIR/omg-moda-front"

echo "========================================"
echo " ClothWise - Inicio de servicios"
echo "========================================"
echo

if [[ ! -d "$BACKEND_DIR" ]]; then
  echo "No se encontro la carpeta del backend: $BACKEND_DIR"
  exit 1
fi

if [[ ! -d "$FRONTEND_DIR" ]]; then
  echo "No se encontro la carpeta del frontend: $FRONTEND_DIR"
  exit 1
fi

if ! command -v java >/dev/null 2>&1; then
  echo "Java no esta instalado o no esta en el PATH. Se requiere Java 21."
  exit 1
fi

if ! command -v npm >/dev/null 2>&1; then
  echo "npm no esta instalado o no esta en el PATH. Instala Node.js y npm."
  exit 1
fi

cleanup() {
  echo
  echo "Deteniendo servicios..."
  kill "${BACKEND_PID:-}" "${FRONTEND_PID:-}" >/dev/null 2>&1 || true
}

trap cleanup INT TERM EXIT

echo "Iniciando backend Spring Boot en http://localhost:8080"
(
  cd "$BACKEND_DIR"
  if [[ -f "./mvnw" ]]; then
    chmod +x ./mvnw >/dev/null 2>&1 || true
    ./mvnw spring-boot:run
  else
    mvn spring-boot:run
  fi
) &
BACKEND_PID=$!

echo "Iniciando frontend Vite en http://localhost:5173"
(
  cd "$FRONTEND_DIR"
  if [[ ! -d "node_modules" ]]; then
    echo "Instalando dependencias del frontend..."
    npm install
  fi
  npm run dev -- --host 0.0.0.0
) &
FRONTEND_PID=$!

echo
echo "Servicios iniciados."
echo "Backend:  http://localhost:8080"
echo "Frontend: http://localhost:5173"
echo
echo "Presiona Ctrl+C para detener ambos servicios."

wait -n "$BACKEND_PID" "$FRONTEND_PID"
