package by.vsdev.posterminal.demo.locale

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

/**
 * Minimal per-app language for English/Spanish without AppCompat: the choice is persisted and
 * applied by wrapping the Activity's base context with a locale-overridden [Configuration].
 * Toggling recreates the Activity so Compose re-reads the localized resources.
 */
object AppLocale {
    const val EN = "en"
    const val ES = "es"
    private val supported = listOf(EN, ES)

    private const val PREFS = "app_locale"
    private const val KEY_LANG = "lang"

    /** The persisted language, defaulting to the system language if supported, else English. */
    fun current(context: Context): String {
        val stored = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_LANG, null)
        if (stored != null) return stored
        return Locale.getDefault().language.takeIf { it in supported } ?: EN
    }

    /** Flips en↔es, persists it, and returns the new language. */
    fun toggle(context: Context): String {
        val next = if (current(context) == EN) ES else EN
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY_LANG, next).apply()
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
