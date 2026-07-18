package by.vsdev.posterminal.demo.locale

import android.content.Context
import android.content.res.Configuration
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.Locale

private val Context.localeDataStore: DataStore<Preferences> by preferencesDataStore(name = "app_locale")

/**
 * Minimal per-app language for English/Spanish without AppCompat: the choice is persisted in a
 * Preferences [DataStore] and applied by wrapping the Activity's base context with a
 * locale-overridden [Configuration]. Toggling recreates the Activity so Compose re-reads the
 * localized resources.
 *
 * The language must be resolved synchronously from `attachBaseContext` (before any coroutine scope
 * exists), so the DataStore read/write is bridged with [runBlocking] — the standard pattern for
 * locale resolution at process start.
 */
object AppLocale {
    const val EN = "en"
    const val ES = "es"
    private val supported = listOf(EN, ES)

    private val KEY_LANG = stringPreferencesKey("lang")

    /** The persisted language, defaulting to the system language if supported, else English. */
    fun current(context: Context): String {
        val stored = runBlocking { context.localeDataStore.data.first()[KEY_LANG] }
        if (stored != null) return stored
        return Locale.getDefault().language.takeIf { it in supported } ?: EN
    }

    /** Flips en↔es, persists it, and returns the new language. */
    fun toggle(context: Context): String {
        val next = if (current(context) == EN) ES else EN
        runBlocking { context.localeDataStore.edit { it[KEY_LANG] = next } }
        return next
    }

    /** Wraps [base] so its resources resolve in the selected locale. Call from attachBaseContext. */
    fun wrap(base: Context): Context {
        val locale = Locale(current(base))
        Locale.setDefault(locale)
        val config = Configuration(base.resources.configuration)
        config.setLocale(locale)
        return base.createConfigurationContext(config)
    }
}
