#!/usr/bin/env bash
set -e

VAC=http://localhost:8081/api/vacunas/aplicaciones
HOY=$(date +%F)
PACIENTE="NINO-$(date +%s)"
HACE_40=$(date -v-40d +%F 2>/dev/null || date -d "-40 days" +%F)
HACE_30=$(date -v-30d +%F 2>/dev/null || date -d "-30 days" +%F)

paso() { printf "\n\033[1;34m== %s ==\033[0m\n" "$1"; }

paso "1. Dosis válida: PENTAVALENTE dosis 1 hace 40 días (publica DosisAplicada)"
curl -s -X POST $VAC -H 'Content-Type: application/json' \
  -d "{\"pacienteId\":\"$PACIENTE\",\"vacuna\":\"PENTAVALENTE\",\"numeroDosis\":1,\"fechaAplicacion\":\"$HACE_40\"}"

paso "2. Regla de negocio: dosis 2 hace 30 días queda a solo 10 días de la dosis 1 (mínimo 28) (no se publica nada)"
curl -s -X POST $VAC -H 'Content-Type: application/json' \
  -d "{\"pacienteId\":\"$PACIENTE\",\"vacuna\":\"PENTAVALENTE\",\"numeroDosis\":2,\"fechaAplicacion\":\"$HACE_30\"}"

paso "3. Dosis 2 hoy: 40 días después (intervalo cumplido)"
curl -s -X POST $VAC -H 'Content-Type: application/json' \
  -d "{\"pacienteId\":\"$PACIENTE\",\"vacuna\":\"PENTAVALENTE\",\"numeroDosis\":2,\"fechaAplicacion\":\"$HOY\"}"

sleep 1

paso "4. Consumidor 1 — notificaciones a acudientes"
curl -s http://localhost:8082/api/notificaciones

paso "5. Consumidor 2 — reportes a PAIWEB"
curl -s http://localhost:8083/api/paiweb/reportes
echo
