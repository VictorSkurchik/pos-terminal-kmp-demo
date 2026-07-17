# Best Practices and Security Alignment Update: Android Intent Security

Audit of the Android app + `feature/mdm` against the `android-intent-security` skill.
minSdk 24, compileSdk 36, AndroidX Core available (`IntentSanitizer` usable if ever needed).

## Summary

**The app already conforms to the intent-security best practices. No code changes were required.**
Adding `IntentSanitizer`, signature verification, or `PendingIntent` hardening where there is no
nested-intent, no bound service, and no `PendingIntent` would be dead code, so none was added.

## Components reviewed

| Component | Exported | Assessment |
|---|---|---|
| `MainActivity` (`app/androidApp`) | `true` (LAUNCHER) | Reads **no** incoming intent extras; no `onNewIntent`, no `getParcelableExtra`/`getStringExtra`, no nested-intent redirection. Export is required for a launcher. **Safe.** |
| `PosDeviceAdminReceiver` (`feature/mdm`) | `true` | Gated by `android:permission="android.permission.BIND_DEVICE_ADMIN"` (system-only) and the `DEVICE_ADMIN_ENABLED` **protected broadcast** (only the framework can send it). Export is required for device-admin receivers. **Compliant** with the skill's Protected-Broadcast + permission guidance. |
| `MdmAgentService` (`feature/mdm`) | `false` | `onBind` returns `null`; started via an explicit same-app `Intent`; handles a null `intent` safely. **Safe.** |
| `InitializationProvider` (androidx.startup) | `false` | Framework-managed, not exported. **Safe.** |

## Intent construction / handling reviewed

- **`SettingsScreen.addAdminIntent()`** builds `Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)`
  with an **explicit** `EXTRA_DEVICE_ADMIN = PosDeviceAdminReceiver.componentName(context)` (our own
  component) and launches it via `ActivityResultContracts.StartActivityForResult`. The result path
  (`SettingsIntent.AdminResult`) re-checks admin state and reads **no** untrusted returned extras.
  This is the standard, safe device-admin enrollment pattern. **Safe.**
- **No `PendingIntent` anywhere** — notifications in `CommandExecutor` and `MdmAgentService` carry no
  `contentIntent`, so the mutable/implicit-PendingIntent antipattern cannot occur.
- **No exported `ContentProvider`, no `sendBroadcast`/sticky broadcast, no dynamic receivers.**
- The QR-scanned payload (`RegisterWithToken`) is treated as an opaque enrollment-token **string**
  sent to the backend — it is never used to construct or launch an `Intent`, so no redirection risk.

## Priority

**Low** — this is a confirmation of existing alignment, not a remediation.

## Forward-looking guardrails (no action now)

- If a notification ever gains a tap action, create the `PendingIntent` with `FLAG_IMMUTABLE`.
- If `MainActivity` ever begins handling deep links / external intent extras, validate them
  (and, for any nested `Intent` extra, use `androidx.core.content.IntentSanitizer`).
- If `MdmAgentService` ever exposes a real binder to other apps, verify the caller via
  `Binder.getCallingUid()` + `PackageManager.hasSigningCertificate(...)` per transaction.

## Testing and verification

1. `./gradlew :app:androidApp:assembleDevDebug` — builds (no code changed).
2. Manifest review confirms exported flags and permissions as tabulated above.
