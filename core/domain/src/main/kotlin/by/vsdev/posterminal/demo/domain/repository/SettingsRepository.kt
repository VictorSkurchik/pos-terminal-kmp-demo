package by.vsdev.posterminal.demo.domain.repository

import kotlinx.coroutines.flow.Flow

/** Local enrollment state: stable id, enrollment status, backend URL. (Policy flags live in [by.vsdev.posterminal.demo.domain.policy.DevicePolicy].) */
interface SettingsRepository {
    val enrolled: Flow<Boolean>
    val deviceName: Flow<String>

    /** Returns the stable device id, generating and persisting one on first use. */
    suspend fun deviceId(): String

    /** The backend URL to talk to (from QR enrollment, falling back to the build default). */
    suspend fun serverUrl(): String

    /** Persists the backend URL (e.g. taken from a scanned QR before registering). */
    suspend fun setServerUrl(url: String)

    suspend fun setEnrolled(enrolled: Boolean, name: String? = null, serverUrl: String? = null)

    suspend fun setDeviceName(name: String)

    /** Clears enrollment + policy flags, returning the app to Registration. Keeps the device id. */
    suspend fun clearEnrollment()
}
