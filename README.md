# POS + MDM — Kotlin Multiplatform MVP

Portfolio project: an Android POS terminal with a built-in **MDM agent**, plus a backend and web admin
for remote device management. Demonstrates Kotlin Multiplatform, Jetpack Compose, a multi-module Android
architecture, Ktor, Room (KMP), Koin, WorkManager, and Android Enterprise APIs (`DevicePolicyManager`,
screen pinning).

## What it does

- **POS**: product catalog → cart → "payment" stub (Android, Compose).
- **MDM agent**: the device registers with the backend, sends heartbeats, polls a command queue
  (WorkManager) and executes commands.
- **Backend** (Ktor + Room/SQLite): stores devices and the command queue, exposes a REST API.
- **Web admin** (Compose for Web / Kotlin JS): device list + command buttons.

All clients talk through a single **shared KMP module** — models, DTOs and the HTTP client are written once.

## Module architecture

```
:core            KMP (android, jvm, js) — @Serializable models, DTOs, CommandType,
                 Ktor HttpClient (expect/actual engine), PosApiClient. Reused EVERYWHERE.
:server          Ktor (JVM) -> :core. Room + BundledSQLiteDriver (self-contained SQLite), REST API.
:core:ui         Android library — Compose Material3 theme, shared components.
:core:data       Android library — Room(AndroidSQLiteDriver) local DB, DataStore prefs,
                 repositories over PosApiClient, Koin dataModule.
:feature:pos     Android feature — catalog/cart/payment, PosViewModel (MVVM), disabled on RESTRICT_APP.
:feature:mdm     Android feature — DeviceAdminReceiver, CommandExecutor, MdmSyncWorker (WorkManager),
                 enrollment screen + QR scanner (CameraX + ML Kit).
:app:androidApp  Android app — Koin init, MainActivity + navigation, Device Admin, WorkManager.
:app:webApp      Compose Web (Kotlin/JS) -> :core. Admin console.
```

The backend is the single source of truth. Shared reuse points:
- **models/DTOs/`CommandType`/HTTP client** — `:core`, three consumers (server JVM, Android, web JS);
- **Room** — one style on the server (`BundledSQLiteDriver`) and on Android (`AndroidSQLiteDriver`);
- **Koin** — a single DI style across all Android modules (+ the worker factory);
- **`formatCents()`** and other utilities — from `:core`, used by both the Android UI and the web admin.

## Tech stack

Kotlin 2.4, AGP 9 (built-in Kotlin), Compose Multiplatform 1.11, Ktor 3.5 (client + server),
Room 2.8 (KMP), Koin 4.2, WorkManager, kotlinx.serialization, Coroutines/Flow, MVVM.
QR enrollment: CameraX + ML Kit Barcode Scanning (Android), qrose (web QR generator).

## Running

Requires JDK 17+, Android SDK, an emulator/device.

### Everything with one command

```bash
./run.sh                # backend + web admin + Android (if a device is connected)
./run.sh --no-android   # backend + web only
```

The script starts the backend (8080) and web admin (8081), sets up `adb reverse` automatically,
installs and launches the Android app, waits for the services to be ready, and streams logs
(into `.run-logs/`). `Ctrl+C` stops everything and frees the ports.

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
./gradlew :app:webApp:jsBrowserDevelopmentRun
# http://localhost:8081 (port 8081 so it doesn't clash with the backend on 8080)
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
- the web admin generates a QR (`qrose`) with `EnrollmentToken(token, serverUrl)` — "New token" button;
- Android scans it (**CameraX + ML Kit Barcode Scanning**, `:feature:mdm/QrScanner`), parses the token
  with shared code from `:core` (`parseEnrollmentToken`), and self-registers;
- the backend stores the token on the device — visible in `GET /devices` and on the admin card
  ("enrolled via QR token=…").

Encoding/decoding of the payload is the same code in `:core` (`toQrPayload` / `parseEnrollmentToken`,
covered by a `:core:jvmTest` test). Optical scanning must be checked on a real camera (the emulator
cannot "show" a QR to its virtual camera).

## Deployment (cloud)

Both clients are pinned to the backend at `https://pos-terminal-kmp-demo.onrender.com`
(`DEFAULT_BASE_URL` in `:core:data`, `SERVER_URL` in `:app:webApp`).

**Backend → Render** (Docker blueprint):
1. Push this repo to GitHub.
2. On Render: *New → Blueprint*, select the repo. Render reads `render.yaml` and builds `Dockerfile`
   (JDK 21 + Android SDK, produces the Ktor fat jar). The service listens on `$PORT`; Render provides HTTPS.
3. Free plan notes: the SQLite file is ephemeral (resets on redeploy) and the instance cold-starts after
   idle, so the first request can take ~30–60 s. For persistent data, add a Render disk and set
   `DATABASE_PATH=/data/pos.db`.

**Web admin → Vercel** (GitHub Actions, `.github/workflows/deploy-web.yml`):
1. Create a Vercel project once (e.g. `vercel link`, or the dashboard) — output is static, no framework.
2. Add repo secrets in GitHub → *Settings → Secrets → Actions*: `VERCEL_TOKEN`, `VERCEL_ORG_ID`,
   `VERCEL_PROJECT_ID`.
3. On every push to `main` touching the web/shared code, the workflow builds
   `:app:webApp:jsBrowserDistribution` and deploys `app/webApp/build/dist/js/productionExecutable` to Vercel.

`.github/workflows/ci.yml` builds the backend jar, the Android APK, the web bundle and runs the `:core`
test on every push/PR. The Android SDK is set up in CI because `:core` has an Android target.

## Intentionally out of scope for the MVP

Real Device Owner / Zero-Touch, FCM push (polling via WorkManager is used instead),
real payments, full authentication/encryption, cloud deployment.
