package cat.copernic.easytraza.mobile.core.config

import android.content.Context
import android.content.res.Configuration
import androidx.core.content.edit
import java.util.Locale

/**
 * CONFIGURACIÓ DE L'IDIOMA.
 *
 * Gestionada la preferència local de l'idioma de l'aplicació mòbil
 * i aplicada la configuració regional corresponent al context utilitzat.
 *
 * @author Ángel Jurado Herruzo
 */
object LanguagePreferences {

    private const val PREFS_NAME = "easytraza_config"
    private const val KEY_LANGUAGE = "app_language"
    private const val DEFAULT_LANGUAGE = "ca"


    /**
     * GUARDAT DE L'IDIOMA.
     *
     * Desat de forma persistent l'idioma seleccionat per l'usuari
     * perquè es mantingui en futurs accessos a l'aplicació.
     *
     * @param context context utilitzat per accedir a les preferències locals
     * @param language codi de l'idioma que s'ha de desar
     */
    fun saveLanguage(context: Context, language: String) {
        val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        preferences.edit { putString(KEY_LANGUAGE, language) }
    }


    /**
     * OBTENCIÓ DE L'IDIOMA.
     *
     * Recuperat l'idioma desat localment o el català quan encara
     * no existeix cap selecció prèvia.
     *
     * @param context context utilitzat per accedir a les preferències locals
     * @return codi de l'idioma configurat
     */
    fun getLanguage(context: Context): String {
        val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return preferences.getString(KEY_LANGUAGE, DEFAULT_LANGUAGE) ?: DEFAULT_LANGUAGE
    }


    /**
     * APLICACIÓ DE L'IDIOMA.
     *
     * Aplicada al context rebut la configuració regional corresponent
     * a l'idioma que l'usuari té seleccionat a l'aplicació.
     *
     * @param context context sobre el qual s'ha d'aplicar l'idioma
     * @return context configurat amb l'idioma seleccionat
     */
    fun applyLocale(context: Context): Context {
        val locale = Locale.forLanguageTag(getLanguage(context))
        Locale.setDefault(locale)

        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)

        return context.createConfigurationContext(configuration)
    }
}
