# CameraX QR Scanner Review

Review of `feature/mdm/.../enrollment/QrScanner.kt` (+ its host `RegistrationScreen.kt`) against
the `camerax` skill. CameraX `androidx.camera` 1.6.1 (camera2/lifecycle/view) + ML Kit
`barcode-scanning` 17.3.0.

## Applied change

**Release CameraX + ML Kit resources when the scanner closes** (`QrScanner.kt`).
The analysis executor (`Executors.newSingleThreadExecutor()`), the ML Kit `BarcodeScanner`, and the
bound `ProcessCameraProvider` were `remember`ed but never released, leaking a thread and the detector
each time the scanner was opened and dismissed. Added a `DisposableEffect` that, on dispose,
`unbindAll()`s the provider, `shutdown()`s the executor, and `close()`s the scanner.

- **Priority:** Medium (resource leak on a repeatable user flow).
- **Files modified:** `feature/mdm/src/main/kotlin/by/vsdev/posterminal/demo/feature/mdm/enrollment/QrScanner.kt`.

## Already compliant (no change)

- **CAMERA permission** is correctly gated in `RegistrationScreen`: `checkSelfPermission` →
  `RequestPermission` launcher → the scanner is shown only once granted.
- **Main-thread callback:** ML Kit's `addOnSuccessListener` (no explicit executor) dispatches on the
  main thread, so `onResult` → Compose state update is already on the main thread.
- **Builder usage** is correct: `Preview.Builder().build()` and `ImageAnalysis.Builder()...build()`
  with `STRATEGY_KEEP_ONLY_LATEST`; no build-and-discard mistakes.
- **Single-result guard** via the `handled` flag prevents duplicate `onResult` calls.

## Recommended (not applied — needs a new dependency)

**Migrate the manual `ImageAnalysis.Analyzer` to CameraX `MlKitAnalyzer`.** The current code manually
pulls `imageProxy.image`, builds `InputImage.fromMediaImage(...)`, and closes the proxy under
`@SuppressLint("UnsafeOptInUsageError")`. `MlKitAnalyzer` (from `androidx.camera:camera-mlkit-vision`)
handles the ImageProxy lifecycle, rotation/coordinate transforms, and proxy closing automatically, and
removes the unsafe opt-in. It was **not** applied here because it requires adding the
`androidx.camera:camera-mlkit-vision` dependency (a catalog change compatible with camera 1.6.x),
which is out of scope for this review branch. Suggested follow-up:

1. Add `androidx.camera:camera-mlkit-vision` to the version catalog (align with `androidx-camera`).
2. Replace `analysis.setAnalyzer(executor) { processImage(...) }` with an `MlKitAnalyzer` bound to the
   `BarcodeScanning` client, delivering results on the main executor.
3. Drop `processImage(...)` and the `@SuppressLint("UnsafeOptInUsageError")`.

## Verification

- `./gradlew :feature:mdm:compileDebugKotlin :feature:mdm:testDebugUnitTest :app:androidApp:assembleDevDebug` — pass.
- Manual: open Registration → Scan QR, scan an enrollment code, then cancel/return repeatedly and
  confirm no thread accumulation (the executor is now shut down on dispose).
