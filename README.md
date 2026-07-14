# POS + MDM — Kotlin Multiplatform MVP

[![CI](https://github.com/VictorSkurchik/pos-terminal-kmp-demo/actions/workflows/ci.yml/badge.svg)](https://github.com/VictorSkurchik/pos-terminal-kmp-demo/actions/workflows/ci.yml)
[![Web admin — Vercel](https://img.shields.io/badge/web%20admin-Vercel-000?logo=vercel)](https://pos-terminal-kmp-demo.vercel.app/)
[![Backend — Render](https://img.shields.io/badge/backend-Render-46E3B7?logo=render&logoColor=white)](https://pos-terminal-kmp-demo.onrender.com/devices)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.4-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)

**Live demo:** [web admin](https://pos-terminal-kmp-demo.vercel.app/) · [backend API](https://pos-terminal-kmp-demo.onrender.com/devices)
— free tier, so the first request may cold-start (~30–60 s).

Portfolio project: an Android POS terminal with a built-in **MDM agent**, plus a backend and web admin
for remote device management. Demonstrates Kotlin Multiplatform, Jetpack Compose, a multi-module Android
architecture, Ktor, Room (KMP), Koin, WorkManager, and Android Enterprise APIs (`DevicePolicyManager`,
screen pinning).

## What it does

- **POS**: product catalog → cart → "payment" stub (Android, Compose).
- **MDM agent**: the device registers with the backend and runs an always-on **foreground service**
  that heartbeats, polls the command queue and surfaces commands (snackbar in-app, notification when
  backgrounded) — so it keeps receiving even when the app isn't on screen. WorkManager is the periodic
  background fallback.
- **Backend** (Ktor + Room/SQLite): stores devices and the command queue, exposes a REST API.
- **Web admin** (React + TypeScript, Vite): device list + command buttons + QR generator.

The backend, the Android app and the **shared KMP `:core`** module reuse the same models, DTOs and Ktor
HTTP client. The web admin is a small separate TS app that mirrors the handful of DTO types.

## Module architecture

```
:core            KMP (android, jvm) — @Serializable models, DTOs, CommandType,
                 Ktor HttpClient, PosApiClient. Shared by :server and the Android app.
:server          Ktor (JVM) -> :core. Room + BundledSQLiteDriver (self-contained SQLite), REST API.
:core:ui         Android library — Compose Material3 theme, shared components.
:core:data       Android library — Room(AndroidSQLiteDriver) local DB, DataStore prefs,
                 repositories over PosApiClient, Koin dataModule.
:feature:pos     Android feature — catalog/cart/payment, PosViewModel (MVVM), disabled on RESTRICT_APP.
:feature:mdm     Android feature — MdmAgentService (always-on foreground service), DeviceAdminReceiver,
                 CommandExecutor, MdmSyncWorker (WorkManager), enrollment + QR scanner (CameraX + ML Kit).
:app:androidApp  Android app — Koin init, MainActivity + navigation, Device Admin, WorkManager.
web-admin/       React + TypeScript (Vite) — admin console. Not KMP; mirrors :core DTOs in TS.
```

The backend is the single source of truth. Shared reuse points:
- **models/DTOs/`CommandType`/HTTP client** — `:core`, two consumers (server JVM, Android);
- **Room** — one style on the server (`BundledSQLiteDriver`) and on Android (`AndroidSQLiteDriver`);
- **Koin** — a single DI style across all Android modules (+ the worker factory).

## Tech stack

Kotlin 2.4, AGP 9 (built-in Kotlin), Compose Multiplatform 1.11 (Android), Ktor 3.5 (client + server),
Room 2.8 (KMP), Koin 4.2, WorkManager, kotlinx.serialization, Coroutines/Flow, MVVM.
Web admin: React 19 + TypeScript + Vite. QR enrollment: CameraX + ML Kit Barcode Scanning (Android),
qrcode.react (web QR generator).

## Running

Requires JDK 17+, Android SDK, an emulator/device.

### Everything with one command

```bash
./run.sh                # backend + web admin + Android (if a device is connected)
./run.sh --no-android   # backend + web only
```

The script starts the backend (8080) and the web admin (Vite dev on 5173, pointed at the local backend
via `VITE_SERVER_URL`), sets up `adb reverse` automatically, installs and launches the Android app, waits
for the services, and streams logs (into `.run-logs/`). `Ctrl+C` stops everything and frees the ports.

Below is how to run each part separately.

### 1. Backend

```bash
./gradlew :server:run
# listens on http://0.0.0.0:8080, creates pos.db (SQLite) in the working directory
```

### 2. Android app

```bash
adb reverse tcp:8080 tcp:8080          # device localhost -> backend on the host
./gradlew :app:androidApp:installDebug
```

In the app: the **Manage** tab → set a name → **Enroll** (the device appears in `GET /devices`),
or **Scan QR to enroll** — point the camera at the QR from the web admin (auto-registration by token).
The **Enable Device Admin** button is needed for a real `LOCK`. **Sync now** runs the agent immediately
(otherwise WorkManager polls every ~15 min).

### 3. Web admin

```bash
cd web-admin
npm install
VITE_SERVER_URL=http://localhost:8080 npm run dev   # http://localhost:5173
# omit VITE_SERVER_URL to target the deployed Render backend
```

The device list refreshes every 3s; the buttons send commands to the selected device.

## REST API

| Method | Path | Purpose |
|--------|------|---------|
| POST | `/devices/register` | register a device (incl. a QR token) |
| POST | `/devices/{id}/heartbeat` | update lastSeen/status/battery |
| GET  | `/devices/{id}/commands` | device pulls pending commands |
| POST | `/devices/{id}/commands/{cmdId}/ack` | acknowledge execution |
| POST | `/devices/{id}/commands` | admin enqueues a command |
| GET  | `/devices` | list all devices |

Example "walkthrough" without the UI:

```bash
curl -X POST localhost:8080/devices/register -H 'Content-Type: application/json' \
  -d '{"deviceId":"dev-001","name":"Till #1"}'
curl -X POST localhost:8080/devices/dev-001/commands -H 'Content-Type: application/json' \
  -d '{"type":"SHOW_MESSAGE","payload":"Hello"}'
curl localhost:8080/devices/dev-001/commands
```

## MDM commands

| Command | Implementation |
|---------|----------------|
| `LOCK` | real `DevicePolicyManager.lockNow()` (Device Admin) |
| `KIOSK_ON` / `KIOSK_OFF` | real `startLockTask()` / `stopLockTask()` (screen pinning) |
| `SHOW_MESSAGE` | dialog with the admin's text (payload) |
| `RESTRICT_APP` | disables the "Pay" button in POS (payload `on`/`off`) |
| `WIPE` | emulated: clears local data (cart) + resets policies, **not** a factory reset |

## QR enrollment

The full loop is implemented:
- the web admin generates a QR (`qrcode.react`) with an `EnrollmentToken` JSON `{token, serverUrl}` —
  "New token" button;
- Android scans it (**CameraX + ML Kit Barcode Scanning**, `:feature:mdm/QrScanner`), parses the token
  with `:core` (`parseEnrollmentToken`), and self-registers;
- the backend stores the token on the device — visible in `GET /devices` and on the admin card
  ("QR token=…").

Optical scanning must be checked on a real camera (the emulator cannot "show" a QR to its virtual camera).

## Deployment (cloud)

Backend URL is pinned in `DEFAULT_BASE_URL` (`:core:data`) for the app, and defaults in the web admin
(`web-admin/src/api.ts`, overridable via `VITE_SERVER_URL`).

**Backend → Render** (Docker blueprint):
1. On Render: *New → Blueprint*, select the repo. Render reads `render.yaml` and builds `Dockerfile`
   (JDK 21 + Android SDK, produces the Ktor fat jar). The service listens on `$PORT`; Render provides HTTPS.
2. Free plan notes: the SQLite file is ephemeral (resets on redeploy) and the instance cold-starts after
   idle, so the first request can take ~30–60 s. For persistent data, add a Render disk and set
   `DATABASE_PATH=/data/pos.db`.

**Web admin → Vercel** (native, no GitHub Actions):
1. Import the repo in Vercel and set **Root Directory = `web-admin`**. Vercel auto-detects Vite,
   runs `npm install` + `npm run build`, and serves `dist/`.
2. Every push to `main` auto-deploys. No tokens or secrets — the Vercel Git integration handles it.

`.github/workflows/ci.yml` builds the backend jar and the Android APK and runs the `:core` test on every
push/PR. The Android SDK is set up in CI because `:core` has an Android target.

## Intentionally out of scope for the MVP

Real Device Owner / Zero-Touch, FCM push (polling via WorkManager is used instead),
real payments, full authentication/encryption.
