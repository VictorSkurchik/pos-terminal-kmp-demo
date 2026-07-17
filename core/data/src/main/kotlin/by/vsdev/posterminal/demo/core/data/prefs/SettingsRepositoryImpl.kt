package by.vsdev.posterminal.demo.core.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import by.vsdev.posterminal.demo.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "pos_prefs")

/**
 * Local device state via DataStore: stable id, enrollment status, backend URL and MDM policy flags.
 * [defaultServerUrl] comes from the build flavor (BuildConfig.SERVER_URL).
 */
class SettingsRepositoryImpl(
    private val context: Context,
    private val defaultServerUrl: String,
) : SettingsRepository {

    private object Keys {
        val DEVICE_ID = stringPreferencesKey("device_id")
        val DEVICE_NAME = stringPreferencesKey("device_name")
        val ENROLLED = booleanPreferencesKey("enrolled")
        val SERVER_URL = stringPreferencesKey("server_url")
    }

    // Guards read-then-write in deviceId() so two concurrent callers can't mint two ids.
    private val deviceIdMutex = Mutex()

    override val enrolled: Flow<Boolean> = context.dataStore.data.map { it[Keys.ENROLLED] ?: false }
    override val deviceName: Flow<String> = context.dataStore.data.map { it[Keys.DEVICE_NAME] ?: "POS Terminal" }

    override suspend fun deviceId(): String = deviceIdMutex.withLock {
        val existing = context.dataStore.data.map { it[Keys.DEVICE_ID] }.first()
        if (existing != null) return existing
        val generated = "pos-" + UUID.randomUUID().toString().take(8)
        context.dataStore.edit { it[Keys.DEVICE_ID] = generated }
        generated
    }

    override suspend fun serverUrl(): String =
        context.dataStore.data.map { it[Keys.SERVER_URL] }.first() ?: defaultServerUrl

    override suspend fun setServerUrl(url: String) {
        context.dataStore.edit { it[Keys.SERVER_URL] = url }
    }

    override suspend fun setEnrolled(enrolled: Boolean, name: String?, serverUrl: String?) {
        context.dataStore.edit { prefs ->
            prefs[Keys.ENROLLED] = enrolled
            name?.let { prefs[Keys.DEVICE_NAME] = it }
            serverUrl?.let { prefs[Keys.SERVER_URL] = it }
        }
    }

    override suspend fun setDeviceName(name: String) {
        context.dataStore.edit { it[Keys.DEVICE_NAME] = name }
    }

    /** Clears enrollment but KEEPS the stable device id, so re-enrolment reuses it. (Policy flags are reset via DevicePolicy.) */
    override suspend fun clearEnrollment() {
        context.dataStore.edit { prefs ->
            prefs.remove(Keys.ENROLLED)
            prefs.remove(Keys.DEVICE_NAME)
            prefs.remove(Keys.SERVER_URL)
        }
    }
}
