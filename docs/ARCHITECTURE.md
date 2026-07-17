# Architecture

The Android app follows a pragmatic **Clean Architecture**: a framework-free domain core, a data
layer that implements the domain contracts, and thin feature/presentation modules on top. Modules
depend **inwards only** — features and UI know the domain, never the other way round.

## Module graph

```
                       ┌─────────────────────────┐
                       │        :app:androidApp   │  Koin init, type-safe NavHost, AppViewModel
                       └───────────┬─────────────┘
              ┌────────────────────┼─────────────────────┐
      ┌───────▼──────┐     ┌───────▼──────┐       ┌───────▼──────┐
      │ :feature:pos │     │ :feature:mdm │       │:feature:offer│  ViewModels + stateless screens
      └───────┬──────┘     └───────┬──────┘       └───────┬──────┘
              └──────────┬─────────┴───────────┬──────────┘
                  ┌───────▼──────┐      ┌───────▼──────┐
                  │  :core:ui    │      │ :core:data   │  Compose design system  /  impls + Room + DataStore + Ktor
                  └───────┬──────┘      └───────┬──────┘
                          └──────────┬──────────┘
                             ┌───────▼───────┐        ┌──────────┐
                             │ :core:domain  │◀───────│  :core   │  wire DTOs + PosApi (shared with :server)
                             └───────────────┘        └──────────┘
```

- **`:core:domain`** (pure Kotlin/JVM) — domain models, `AppResult`/`DomainError`, repository & service
  **interfaces**, `DispatcherProvider`, and thin **use cases**. No Android, Ktor, Room or Koin.
- **`:core`** (KMP) — `@Serializable` **wire DTOs**, the `PosApi` interface + `KtorPosApiClient`. Shared
  with the Ktor `:server` and the web admin, so it stays transport-only.
- **`:core:data`** (Android) — implements the domain repositories, **maps DTO ↔ domain**, owns Room,
  DataStore and the Koin `dataModule`/`domainModule`. The only place that knows about Ktor exceptions.
- **`:core:ui`** — Material3 theme + atomic-design components (atoms/molecules/organisms), with previews.
- **`:feature:*`** — a `ViewModel` per screen over **use cases** (never repositories directly) plus
  stateless composables. MDM infra (foreground service, WorkManager, `DevicePolicyManager`) lives here
  behind the domain service interfaces.
- **`:app:androidApp`** — Koin startup, the type-safe `NavHost`, and `AppViewModel` (session/kiosk logic).

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
