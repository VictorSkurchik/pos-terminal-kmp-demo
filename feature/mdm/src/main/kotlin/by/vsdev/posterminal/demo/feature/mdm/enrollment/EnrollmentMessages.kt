package by.vsdev.posterminal.demo.feature.mdm.enrollment

import android.content.Context
import by.vsdev.posterminal.demo.domain.result.DomainError
import by.vsdev.posterminal.demo.feature.mdm.R

/** Maps registration side effects to user-facing, localized text (kept out of the ViewModel). */
fun RegistrationSideEffect.toMessage(context: Context): String = when (this) {
    is RegistrationSideEffect.Enrolled -> context.getString(R.string.enroll_success, deviceId)
    RegistrationSideEffect.InvalidQr -> context.getString(R.string.enroll_invalid_qr)
    is RegistrationSideEffect.Failed -> error.toMessage(context)
    RegistrationSideEffect.LaunchDeviceAdmin -> ""
}

/** Maps settings side effects to localized text. */
fun SettingsSideEffect.toMessage(context: Context): String = when (this) {
    SettingsSideEffect.SyncRequested -> context.getString(R.string.enroll_sync_requested)
    SettingsSideEffect.LoggedOut -> context.getString(R.string.enroll_logged_out)
    is SettingsSideEffect.Failed -> error.toMessage(context)
}

fun DomainError.toMessage(context: Context): String = when (this) {
    DomainError.Network -> context.getString(R.string.err_network)
    DomainError.Timeout -> context.getString(R.string.err_timeout)
    DomainError.Unauthorized -> context.getString(R.string.err_unauthorized)
    DomainError.NotFound -> context.getString(R.string.err_not_found)
    is DomainError.Server -> context.getString(R.string.err_server, code)
    DomainError.Serialization -> context.getString(R.string.err_serialization)
    is DomainError.Unknown -> context.getString(R.string.err_unknown)
}
