package by.vsdev.posterminal.demo.core.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "pos_prefs")

/** Local device state: stable id, enrollment status, MDM policy flags. */
class SettingsRepository(private val context: Context) {

    private object Keys {
        val DEVICE_ID = stringPreferencesKey("device_id")
        val DEVICE_NAME = stringPreferencesKey("device_name")
        val ENROLLED = booleanPreferencesKey("enrolled")
        val SERVER_URL = stringPreferencesKey("server_url")
        val RESTRICT_APP = booleanPreferencesKey("restrict_app")
    }

    val enrolled: Flow<Boolean> = context.dataStore.data.map { it[Keys.ENROLLED] ?: false }
    val deviceName: Flow<String> = context.dataStore.data.map { it[Keys.DEVICE_NAME] ?: "POS Terminal" }
    val restrictApp: Flow<Boolean> = context.dataStore.data.map { it[Keys.RESTRICT_APP] ?: false }

    /** Returns the existing deviceId or generates and stores a new one. */
    suspend fun getOrCreateDeviceId(): String {
        val existing = context.dataStore.data.map { it[Keys.DEVICE_ID] }.first()
        if (existing != null) return existing
        val generated = "pos-" + UUID.randomUUID().toString().take(8)
        context.dataStore.edit { it[Keys.DEVICE_ID] = generated }
        return generated
    }

    suspend fun serverUrl(): String = context.dataStore.data.map { it[Keys.SERVER_URL] }.first() ?: DEFAULT_SERVER_URL

    suspend fun setEnrolled(value: Boolean, name: String? = null, serverUrl: String? = null) {
        context.dataStore.edit { prefs ->
            prefs[Keys.ENROLLED] = value
            name?.let { prefs[Keys.DEVICE_NAME] = it }
            serverUrl?.let { prefs[Keys.SERVER_URL] = it }
        }
    }

    suspend fun setRestrictApp(value: Boolean) {
        context.dataStore.edit { it[Keys.RESTRICT_APP] = value }
    }

    suspend fun setDeviceName(name: String) {
        context.dataStore.edit { it[Keys.DEVICE_NAME] = name }
    }

    /** Full wipe of local state (WIPE emulation). */
    suspend fun clearAll() {
        context.dataStore.edit { it.clear() }
    }

    companion object {
        const val DEFAULT_SERVER_URL = "http://localhost:8080"
    }
}
