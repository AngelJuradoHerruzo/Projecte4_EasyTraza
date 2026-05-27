package cat.copernic.easytraza.mobile.core.config

import android.content.Context
import android.content.res.Configuration
import androidx.core.content.edit
import java.util.Locale

object LanguagePreferences {

    private const val PREFS_NAME = "easytraza_config"
    private const val KEY_LANGUAGE = "app_language"
    private const val DEFAULT_LANGUAGE = "ca"


    fun saveLanguage(context: Context, language: String) {
        val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        preferences.edit { putString(KEY_LANGUAGE, language) }
    }

    fun getLanguage(context: Context): String {
        val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return preferences.getString(KEY_LANGUAGE, DEFAULT_LANGUAGE) ?: DEFAULT_LANGUAGE
    }

    fun applyLocale(context: Context): Context {
        val locale = Locale.forLanguageTag(getLanguage(context))
        Locale.setDefault(locale)

        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)

        return context.createConfigurationContext(configuration)
    }
}
