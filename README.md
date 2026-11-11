# Esqueleto funcional — Seguimiento pediátrico (vacunación)

Walking skeleton del proyecto integrador. Materializa el estilo **orientado a eventos**: un servicio publica el evento `DosisAplicada` y dos consumidores, en procesos separados, reaccionan cada uno por su lado.

```
                         ┌──────────────────────────────┐
POST /api/vacunas/  ──►  │ servicio-vacunacion  (8081)  │
  aplicaciones           │ valida esquema → publica     │
                         └──────────────┬───────────────┘
                                        │ DosisAplicada
                                        ▼  exchange topic "pediatria.eventos"
                          ┌─────────────── RabbitMQ ───────────────┐
                          │                                        │
              cola notificaciones.dosis-aplicada     cola paiweb.dosis-aplicada (+ DLQ)
                          │                                        │
                          ▼                                        ▼
        ┌──────────────────────────────┐      ┌──────────────────────────────┐
        │ servicio-notificaciones(8082)│      │ servicio-reporte-paiweb(8083)│
        │ avisa al acudiente           │      │ reporta a PAIWEB (simulado), │
        └──────────────────────────────┘      │ reintentos + idempotencia    │
                                              └──────────────────────────────┘
```

## Requisitos

- Docker Desktop (en Mac: con el motor encendido).
- Nada más: Java y Maven corren dentro de los contenedores.

## Levantar todo (un comando)

```bash
docker compose up --build
```

La primera vez tarda unos minutos porque descarga las dependencias de Maven. Cuando en los logs aparezcan los tres `Started ...Application`, ya está listo.

## Demostración

En otra terminal:

```bash
./demo.sh
```

El script:

1. Registra una dosis válida → se publica el evento.
2. Intenta una dosis que viola el intervalo mínimo → responde **422** y **no** se publica nada (la regla vive en el dominio del publicador).
3. Registra una segunda dosis válida.
4. Muestra que **notificaciones** recibió ambos eventos.
5. Muestra que **reporte-paiweb** también los recibió, de forma independiente.

Consola de RabbitMQ: http://localhost:15672 (usuario `guest`, clave `guest`). En **Queues** se ven las dos colas, cada una con su consumidor.

## Demostración de tolerancia a fallos (driver de interoperabilidad)

**Caso A — un consumidor caído no afecta al publicador ni al otro consumidor:**

```bash
docker compose stop reporte-paiweb
./demo.sh                              # notificaciones sigue funcionando
docker compose start reporte-paiweb    # al volver, procesa los eventos que esperaban en su cola
curl -s localhost:8083/api/paiweb/reportes
```

> Al reiniciar, reporte-paiweb pierde lo que tenía en memoria, así que la lista mostrará solo los eventos que procesó después de volver. Lo que se demuestra es que esos eventos no se perdieron mientras estuvo caído.

**Caso B — PAIWEB (sistema externo) no responde:**

```bash
curl -s -X POST "localhost:8083/api/paiweb/disponibilidad?valor=false"
./demo.sh
docker compose logs -f reporte-paiweb   # 4 intentos con espera creciente (2s, 4s, 8s)
```

Tras agotar los reintentos, el mensaje pasa a la cola `paiweb.dosis-aplicada.dlq` (visible en la consola de RabbitMQ) en lugar de perderse. Para restablecer:

```bash
curl -s -X POST "localhost:8083/api/paiweb/disponibilidad?valor=true"
```

## Endpoints

| Servicio | Método | Ruta |
|---|---|---|
| vacunación | POST | `/api/vacunas/aplicaciones` — body: `pacienteId`, `vacuna`, `numeroDosis`, `fechaAplicacion` (opcional) |
| notificaciones | GET | `/api/notificaciones` |
| reporte-paiweb | GET | `/api/paiweb/reportes` |
| reporte-paiweb | POST | `/api/paiweb/disponibilidad?valor=true\|false` |

Vacunas en el esquema de ejemplo: `BCG`, `HEPATITIS_B`, `PENTAVALENTE`, `POLIO`, `NEUMOCOCO`, `TRIPLE_VIRAL`. Los intervalos son ilustrativos, no el esquema oficial del PAI.

## Cómo corresponde al diseño

| Decisión del diseño | Dónde se ve en el código |
|---|---|
| Estilo orientado a eventos | Exchange `pediatria.eventos`; `PublicadorRabbit` publica, dos `@RabbitListener` consumen |
| Un contexto por servicio, sin código compartido | Cada consumidor tiene **su propia copia** de `DosisAplicada`; el contrato es el JSON, no una librería común |
| Correctitud clínica en el núcleo | `EsquemaVacunacion` (paquete `dominio`) sin dependencias de Spring ni de RabbitMQ |
| Puertos y adaptadores en el publicador | `aplicacion` define `HistorialDosis` y `PublicadorEventos`; `infraestructura` los implementa |
| Tolerancia a fallos de PAIWEB | Reintentos con backoff, cola de mensajes muertos (DLQ) y colas durables |
| Entrega al menos una vez → idempotencia | `ReportePaiwebListener` ignora `eventoId` ya procesados |

## Simplificaciones del esqueleto

- El historial de dosis y las listas de los consumidores viven en memoria (se pierden al reiniciar).
- PAIWEB está simulado; el envío de notificaciones solo escribe en el log.
- No hay autenticación ni base de datos todavía.

## Estructura

```
pediatria-esqueleto/
├── docker-compose.yml
├── demo.sh
├── servicio-vacunacion/       dominio · aplicacion · infraestructura · web
├── servicio-notificaciones/
└── servicio-reporte-paiweb/
```
