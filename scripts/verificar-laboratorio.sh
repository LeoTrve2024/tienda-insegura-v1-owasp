#!/usr/bin/env bash
set -euo pipefail

BASE_URL="http://127.0.0.1:8080"
FRONT_URL="http://127.0.0.1:3000"

echo "[1/5] Docker Compose"
docker compose ps

echo "[2/5] Backend health"
curl --fail --silent --show-error "$BASE_URL/actuator/health"
echo

echo "[3/5] Productos"
curl --fail --silent --show-error "$BASE_URL/api/productos" >/dev/null
echo "OK"

echo "[4/5] Frontend"
curl --fail --silent --show-error --head "$FRONT_URL" >/dev/null
echo "OK"

echo "[5/5] Base de datos"
docker exec tienda_v1_postgres \
  psql -U tienda_user -d tienda_insegura \
  -tAc "SELECT COUNT(*) FROM usuarios;" |
  sed 's/^/Usuarios: /'

echo "[OK] Laboratorio disponible en localhost."
