package by.vsdev.posterminal.demo.policy

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import by.vsdev.posterminal.demo.domain.policy.DevicePolicy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.devicePolicyStore: DataStore<Preferences> by preferencesDataStore(name = "device_policy_prefs")

/** [DevicePolicy] backed by a small DataStore, owned by the app (the composition root). */
class DevicePolicyStore(private val context: Context) : DevicePolicy {

    private object Keys {
        val KIOSK_ACTIVE = booleanPreferencesKey("kiosk_active")
    }

    override val kioskActive: Flow<Boolean> =
        context.devicePolicyStore.data.map { it[Keys.KIOSK_ACTIVE] ?: false }

    override suspend fun setKioskActive(value: Boolean) {
        context.devicePolicyStore.edit { it[Keys.KIOSK_ACTIVE] = value }
    }

    override suspend fun reset() {
        context.devicePolicyStore.edit { it.clear() }
    }
}
