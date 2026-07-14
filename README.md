# Restaurant POS + MDM — Kotlin Multiplatform

[![CI](https://github.com/VictorSkurchik/pos-terminal-kmp-demo/actions/workflows/ci.yml/badge.svg)](https://github.com/VictorSkurchik/pos-terminal-kmp-demo/actions/workflows/ci.yml)
[![Web admin — Vercel](https://img.shields.io/badge/web%20admin-Vercel-000?logo=vercel)](https://pos-terminal-kmp-demo.vercel.app/)
[![Backend — Render](https://img.shields.io/badge/backend-Render-46E3B7?logo=render&logoColor=white)](https://pos-terminal-kmp-demo.onrender.com/devices)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.4-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)

**Live demo:** [web admin](https://pos-terminal-kmp-demo.vercel.app/) · [backend API](https://pos-terminal-kmp-demo.onrender.com/devices)
— free tier, so the first request may cold-start (~30–60 s).

Portfolio project: an Android **restaurant POS terminal** with a built-in **MDM agent**, plus a Ktor
backend and a web admin console for remote device management. Demonstrates Kotlin Multiplatform, Jetpack
Compose with a Material3 **atomic-design** component library, multi-module architecture, Navigation
Compose, Ktor, Room (KMP), Koin, WorkManager, a foreground service, and Android Enterprise APIs
(`DevicePolicyManager`, screen pinning).

## What it does

- **POS** (Android, Compose): branded menu grid (2 columns, image/emoji cards) → cart → "payment" stub.
- **MDM agent**: the terminal registers with the backend and runs an always-on **foreground service**
  that heartbeats, polls the command queue and executes commands — it keeps working when the app is
  backgrounded (surfacing commands as a snackbar in-app, a notification otherwise). WorkManager is the
  periodic fallback.
- **Offer attract loop**: in kiosk mode, after 10 s of inactivity the terminal shows a full-screen
  promo screensaver (rotating offers with Instagram-stories progress bars); a tap returns to POS.
- **Backend** (Ktor + Room/SQLite): source of truth for devices and the command queue; REST API.
- **Web admin** (React + TypeScript, Vite): device list with live status badges, grouped command
  buttons, and a QR generator for enrollment.

The backend, the Android app and the **shared KMP `:core`** module reuse the same models, DTOs and Ktor
HTTP client. The web admin is a small separate TS app that mirrors the handful of DTO types.

## Screens & navigation

Navigation Compose (`org.jetbrains.androidx.navigation`, the Compose-Multiplatform build of Google's
Navigation Compose). The app owns the `NavHost`; feature modules expose stateless screens + callbacks.

```
Registration ──enroll (QR / manual)──▶ POS ──gear icon──▶ Settings
     ▲                                  │                    │
     └──────── Factory reset / Wipe ────┴────────────────────┘
                                        │
                    kiosk idle 10 s ──▶ Offer ──tap──▶ POS
```

Start destination is enrolment-driven: unenrolled → **Registration**, enrolled → **POS**.

## Module architecture

```
:core            KMP (android, jvm) — @Serializable models, DTOs, CommandType,
                 Ktor HttpClient, PosApiClient. Shared by :server and the Android app.
:server          Ktor (JVM) -> :core. Room + BundledSQLiteDriver (self-contained SQLite), REST API.
:core:ui         Android library — Material3 theme (mint palette/type/shapes) + atomic-design
                 components: atoms/molecules/organisms (AppButton, ProductCard, MenuGrid,
                 CartPanel, StoryProgressBar, ConfirmDialog, …). Coil for images.
:core:data       Android library — Room(AndroidSQLiteDriver) local DB, DataStore prefs,
                 repositories over PosApiClient, Koin dataModule.
:feature:pos     Android feature — reworked POS screen (top bar + 2-col grid + transparent cart).
:feature:mdm     Android feature — MdmAgentService (always-on foreground service), CommandExecutor,
                 DeviceAdminReceiver, MdmSyncWorker, Registration + Settings screens, QR scanner.
:feature:offer   Android feature — full-screen Offer attract loop (stories-style).
:app:androidApp  Android app — Koin init, AppNavHost (routes + kiosk idle), foreground-service start.
web-admin/       React + TypeScript (Vite) — admin console. Not KMP; mirrors :core DTOs in TS.
```

Shared reuse points: models/DTOs/`CommandType`/HTTP client (`:core`); one Room style on server
(`BundledSQLiteDriver`) and Android (`AndroidSQLiteDriver`); one Koin DI style; one design system (`:core:ui`).

## Tech stack

Kotlin 2.4, AGP 9 (built-in Kotlin), Compose Multiplatform 1.11 (Android) + Navigation Compose 2.9,
Ktor 3.5 (client + server), Room 2.8 (KMP), Koin 4.2, WorkManager, Coil 3, material-icons-core,
kotlinx.serialization, Coroutines/Flow, MVVM. Web admin: React 19 + TypeScript + Vite; qrcode.react.

## Running

Requires JDK 17+, Android SDK, an emulator/device.

### Everything with one command

```bash
./run.sh                # backend + web admin + Android (if a device is connected)
./run.sh --no-android   # backend + web only
```

Starts the backend (8080) and the web admin (Vite dev on 5173, pointed at the local backend via
`VITE_SERVER_URL`), sets up `adb reverse`, installs + launches the Android app, and streams logs into
`.run-logs/`. `Ctrl+C` stops everything.

### 1. Backend

```bash
./gradlew :server:run
# listens on http://0.0.0.0:8080, creates pos.db (SQLite) in the working directory
```

### 2. Android app

```bash
adb reverse tcp:8080 tcp:8080          # device localhost -> backend on the host (local dev)
./gradlew :app:androidApp:installDebug
```

On first launch the app shows **Registration** — tap **Scan QR to register** and point the camera at
the QR from the web admin (or **Register manually**). It then lands on **POS**. The **gear icon** opens
**Settings** (device info, **Enable Device Admin** for a real `LOCK`, **Sync now**, and **Factory
reset**). The APK is also downloadable from each CI run's Artifacts.

### 3. Web admin

```bash
cd web-admin
npm install
VITE_SERVER_URL=http://localhost:8080 npm run dev   # http://localhost:5173
# omit VITE_SERVER_URL to target the deployed Render backend
```

## REST API

| Method | Path | Purpose |
|--------|------|---------|
| POST | `/devices/register` | register a device (idempotent upsert; optional QR token) |
| POST | `/devices/{id}/heartbeat` | update lastSeen/status/battery + kiosk/restrict flags |
| GET  | `/devices/{id}/commands` | device pulls pending commands (PENDING → DELIVERED) |
| POST | `/devices/{id}/commands/{cmdId}/ack` | acknowledge execution (→ DONE) |
| POST | `/devices/{id}/commands` | admin enqueues a command |
| GET  | `/devices` | list all devices (with status/kiosk/restrict) |
| DELETE | `/devices/{id}` | remove a device + its commands |

## MDM commands

| Command | Implementation |
|---------|----------------|
| `LOCK` | real `DevicePolicyManager.lockNow()` (requires Device Admin) |
| `KIOSK_ON` / `KIOSK_OFF` | real `startLockTask()` / `stopLockTask()` (screen pinning); while on, the Offer screensaver kicks in after 10 s idle |
| `SHOW_MESSAGE` | AlertDialog with the admin's text (payload) |
| `RESTRICT_APP` | toggles the POS "Pay" button (payload `on` / `off`) |
| `WIPE` | admin reset: the terminal deletes itself from the backend, clears local state, and returns to Registration |

Device state (`kioskActive`, `restrictPayment`) is reported via heartbeat and shown as **status badges**
in the admin. Buttons are grouped: Screen / Message / Payment / Danger zone.

## Resilience (free-tier self-heal)

Render's free tier is ephemeral and spins down when idle — on restart the SQLite DB is wiped and devices
vanish. To avoid silently logging terminals out, the agent **re-registers itself on a `404`** (it still
knows its id/name) and retries, so a device reappears in the admin automatically. Intentional resets are
explicit: **Factory reset** (on-device) and **Wipe** (admin `WIPE` command) both delete the device and
send the app back to Registration. For durable data, attach a Render disk and set `DATABASE_PATH=/data/pos.db`.

## QR enrollment

The web admin generates a QR (`qrcode.react`) encoding `EnrollmentToken {token, serverUrl}`. Android
scans it (**CameraX + ML Kit Barcode Scanning**, `:feature:mdm/QrScanner`), parses it with shared `:core`
code (`parseEnrollmentToken`) and self-registers; the backend stores the token (shown in the admin).
Optical scanning must be checked on a real camera — the emulator can't present a QR to its virtual camera.

## Deployment (cloud)

Backend URL is pinned in `DEFAULT_BASE_URL` (`:core:data`) for the app, and defaults in the web admin
(`web-admin/src/api.ts`, overridable via `VITE_SERVER_URL`).

- **Backend → Render**: *New → Blueprint* on the repo. Render reads `render.yaml` and builds `Dockerfile`
  (JDK 21 + Android SDK → Ktor fat jar); binds `$PORT`, provides HTTPS.
- **Web admin → Vercel**: import the repo, set **Root Directory = `web-admin`**; Vercel auto-detects Vite
  and auto-deploys on push. No secrets.
- **CI** (`.github/workflows/ci.yml`): builds the backend jar + Android APK, runs `:core` tests, and
  uploads the APK artifact. `release.yml` attaches the APK to a GitHub Release on tag `v*`.

## Intentionally out of scope

Real Device Owner / Zero-Touch, FCM push (polling via the foreground service + WorkManager instead),
real payments, full authentication/encryption.
