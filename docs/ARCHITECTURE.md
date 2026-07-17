# Architecture

The app is organised as **feature-owned vertical slices over a small shared kernel**. Each feature
module contains its own `domain` (models, repository/service interfaces, use cases), `data`
(implementations, persistence, transport) and `presentation` (MVI ViewModel + composables), with its
own Koin module. Core holds only what is genuinely shared. Dependencies point **inwards only** — a
feature knows the kernel, never another feature.

## Module graph

```
                       ┌─────────────────────────┐
                       │       :app:androidApp    │  Koin init, NavHost, AppViewModel, DevicePolicy store
                       └───────────┬─────────────┘
              ┌────────────────────┼─────────────────────┐
      ┌───────▼──────┐     ┌───────▼──────┐       ┌───────▼──────┐
      │ :feature:pos │     │ :feature:mdm │       │:feature:offer│  each: domain + data + presentation
      └───┬───────┬──┘     └──┬────────┬──┘       └───────┬──────┘
          │       │           │        │                  │
          ▼       └─────┬─────┘        ▼                  ▼
     ┌─────────┐   ┌────▼─────┐   ┌────────┐         ┌────────┐
     │ :core:  │   │ :core:   │   │ :core  │◀────────│:server │  (wire, shared)
     │ domain  │   │   ui     │   │ (wire) │         └────────┘
     │(kernel) │   │(design)  │   └────────┘
     └─────────┘   └──────────┘
```

- **`:core:domain`** (pure Kotlin/JVM) — the **shared kernel**: `AppResult`/`DomainError`,
  `DispatcherProvider` (+ default), `formatCents`, and the cross-cutting **`DevicePolicy`** port. No
  feature domain, no Android/Ktor/Room/Koin.
- **`:core`** (KMP) — `@Serializable` **wire DTOs** + wire Device/Command models + `PosApi`/
  `KtorPosApiClient` + `posJson`. Transport-only; shared by the Ktor `:server` and `:feature:mdm`.
- **`:core:ui`** — a **generic** Material3 design system (atoms/molecules) + theme + the **MVI base**
  (`MviViewModel`/`UiState`/`UiIntent`/`UiSideEffect`). No domain dependency.
- **`:feature:pos`** — vertical slice: Product/Cart domain + use cases, Room cart store + catalog
  (data), and the POS screen/components (presentation). Owns no shared state.
- **`:feature:mdm`** — vertical slice: Device/Command domain, the `MdmServices` service interfaces and
  use cases; data = `DeviceRepositoryImpl`, enrollment-settings DataStore, Ktor client, device-info/
  time providers, plus the Android infra (foreground service, `DeviceAdminReceiver`, WorkManager,
  CameraX/ML-Kit QR scanner); presentation = Registration + Settings.
- **`:feature:offer`** — presentation-only (the attract-loop screensaver); no domain/data.
- **`:app:androidApp`** — Koin startup, the type-safe `NavHost`, `AppViewModel` (session/kiosk), and the
  `DevicePolicy` DataStore implementation (the one cross-feature policy store lives at the composition root).

### Cross-cutting policy (`DevicePolicy`)
`restrictPayment` and `kioskActive` are set remotely by MDM commands but read by POS (to gate the Pay
button) and the heartbeat. Rather than couple the two features, they sit behind a `DevicePolicy` port in
the kernel; the app provides the concrete DataStore. So `pos` and `mdm` never depend on each other.

## Key conventions

### Error handling
Every IO-performing repository/use case returns `AppResult<T>` (`Success`/`Failure(DomainError)`).
`KtorPosApiClient` enables `expectSuccess`; `DeviceRepositoryImpl` translates transport exceptions into
the typed `DomainError` taxonomy (`Network`, `Timeout`, `Unauthorized`, `NotFound`, `Server(code)`,
`Serialization`, `Unknown`) at the IO boundary, and **re-throws `CancellationException`** so structured
concurrency is never broken. Nothing is swallowed silently; background failures are logged.

### Presentation (MVI)
The presentation layer is **MVI** with a shared base in `:core:ui` —
`MviViewModel<S : UiState, I : UiIntent, E : UiSideEffect>` (`core/ui/.../mvi/Mvi.kt`) enforcing a
unidirectional flow: one immutable `S : UiState` exposed as `StateFlow` (reduced via `setState`),
a single `onIntent(intent: I)` entry point for all user/system actions, and one-shot `E : UiSideEffect`s
delivered exactly once through a `Channel` (`postSideEffect`). Each feature declares its
`…Contract` (State/Intent/SideEffect) — e.g. `RegistrationContract`, `SettingsContract`, plus
`PosViewModel`, `OfferViewModel`, and the app-level `AppViewModel`.

Screens are stateless: they collect `state` with `collectAsStateWithLifecycle()` and forward user
actions through `onIntent`. Side effects (a paid receipt, an admin message, an enrollment result,
launching a system intent) are **semantic** — e.g. `PosEvent.PaymentCompleted(amountCents)` — and the
screen maps them to localized strings. ViewModels hold **no user-facing strings**; all live in
`strings.xml`.

### Navigation
Type-safe Navigation-Compose with `@Serializable` `AppRoute` objects. `AppViewModel` owns the
enrollment-driven start destination, enrollment/logout redirects, and the kiosk idle timer; the NavHost
just performs the emitted `AppNavEvent`s and reports user interaction.

### Dependency injection
Koin, split by concern: `dataModule` (implementations bound to domain interfaces), `domainModule`
(use cases), and per-feature modules. The backend base URL is resolved per-request from settings
(defaulting to the flavor's `BuildConfig.SERVER_URL`), so a QR-scanned `serverUrl` takes effect at runtime.

### Testing
Interfaces + constructor injection + `DispatcherProvider` make the core testable without instrumentation:
use cases and ViewModels run on fakes (+ Turbine), `DeviceRepositoryImpl` against a Ktor `MockEngine`
(covering success, 404 self-heal, and 5xx → typed error), and the cart DAO in-memory.

## Build & tooling
Convention plugins in `build-logic/` remove per-module duplication; ktlint + detekt + Android Lint run in
CI. The JDK toolchain is single-sourced from the version catalog (`jdk`) and consumed by every module and
convention plugin; grouped dependencies (Ktor/Koin/Coil/coroutines) are aligned via version-catalog BOMs.
Three flavors (`dev`/`staging`/`prod`) carry environment config; `release` is minified (R8) and signed.
See `CONTRIBUTING.md` for commit conventions and local commands.
