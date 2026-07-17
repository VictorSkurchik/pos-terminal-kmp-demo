# R8 Keep-Rules Analysis

Advisory report (`r8-analyzer` skill, heuristic path). No code changes — suggestions only.
Target: `app/androidApp/proguard-rules.pro` (release build: `isMinifyEnabled=true`,
`isShrinkResources=true`, `proguard-android-optimize.txt` + `proguard-rules.pro`).

## 1. Configuration

- **AGP Version**: 9.0.1 — already ≥ 9.0. No migration needed.
- **Full Mode**: `android.enableR8.fullMode=false` is **not** present in `gradle.properties` — good (R8 full mode stays enabled).

## 2. Global disable rules

None. No `-dontobfuscate`, `-dontoptimize`, or `-dontshrink` present. Good.

## 3. Optimization summary

Quantitative scores require the R8 configuration analyzer (Path A: R8 ≥ 9.3.7-dev + Python/protobuf),
which was not run here. The findings below are the heuristic (Path B) evaluation of each keep rule.

## 4. Keep rules evaluation

### `-keep class kotlinx.coroutines.** { *; }`
- **Action: Remove.** Blanket keep over all of coroutines is explicitly detrimental — coroutines
  **1.7.0+** bundle their own consumer rules (this project is on **1.11.0**). Keeping every internal
  coroutine API blocks inlining/shrinking of a large surface.

### `-keep class * extends androidx.room.RoomDatabase { <init>(); }` and `-keep @androidx.room.Entity class *`
- **Action: Remove.** Room generates its own keep rules for the code it creates; manual Room/Entity
  keeps are redundant and prevent R8 from optimizing the data-access layer.

### `-keep class io.ktor.** { *; }`
- **Action: Refine.** Blanket library preservation. Prefer relying on Ktor's own consumer rules and
  the existing `-dontwarn io.ktor.**`; if runtime reflection issues appear on the OkHttp engine,
  keep only the specific engine/serialization classes that fail, not the whole `io.ktor.**` tree.

### `-keep class org.koin.** { *; }`
- **Action: Refine (verify).** Broad keep across all of Koin. Koin resolves types reflectively at
  module definition, but a whole-package `{ *; }` keep is wider than needed — verify a release build
  with this narrowed (or removed) and keep only what breaks.

### `-keepclassmembers class **$$serializer { *; }`, `-keepclasseswithmembers class * { @kotlinx.serialization.Serializable <methods>; }`, `-if @kotlinx.serialization.Serializable class ** -keep class <1> { *; }`
- **Action: Refine (verify).** kotlinx-serialization bundles consumer ProGuard rules in modern
  versions (project on **1.11.0**). These manual serializer keeps are likely redundant; verify a
  minified build still (de)serializes DTOs, then drop the ones the bundled rules already cover.
  `-keepattributes *Annotation*, InnerClasses` should stay (needed for reflective serializers/enums).

### `-keep class by.vsdev.posterminal.demo.dto.** { *; }` and `-keep class by.vsdev.posterminal.demo.model.** { *; }`
- **Action: Refine.** Whole-package `{ *; }` data-model keeps are broader than necessary — the
  `-if @kotlinx.serialization.Serializable` rule (or the library's bundled rules) already retains
  what serialization needs. Narrow to only non-`@Serializable` types that are accessed reflectively,
  if any; otherwise remove.

## 5. Subsumed keep rules

- `by.vsdev.posterminal.demo.dto.**` / `model.**` blanket keeps are largely **subsumed by** the
  `-if @kotlinx.serialization.Serializable` rule for the serializable subset. **Action: Remove** the
  overlap once verified.

## Harmless (keep as-is)

`-dontnote kotlinx.serialization.**`, `-dontwarn org.slf4j.**`, `-dontwarn io.ktor.**` — suppress
notes/warnings only; no optimization impact.

## Recommended verification for any change

After narrowing/removing a rule, run `:app:androidApp:assembleProdRelease` (or `Staging`) and
exercise: enrollment (QR + manual), MDM command round-trip (LOCK/KIOSK/SHOW_MESSAGE/WIPE), and DTO
(de)serialization against the backend. A Macrobenchmark before/after (UI Automator) quantifies the
size/startup win.
