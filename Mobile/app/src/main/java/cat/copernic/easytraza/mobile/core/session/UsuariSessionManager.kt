package cat.copernic.easytraza.mobile.core.session

import android.content.Context
import androidx.core.content.edit
import cat.copernic.easytraza.mobile.features.auth.domain.models.UsuariIdentificat

/**
 * Gestor de sessió local de l'usuari identificat.
 */
object UsuariSessionManager {

    private const val PREFS_NAME = "easytraza_session"
    private const val KEY_USUARI_ID = "usuari_id"
    private const val KEY_USUARI_DNI = "usuari_dni"
    private const val KEY_USUARI_NOM = "usuari_nom"
    private const val KEY_USUARI_EMAIL = "usuari_email"
    private const val KEY_USUARI_ROL = "usuari_rol"


    /**
     * Desa l'usuari identificat.
     */
    fun guardarUsuari(context: Context, usuari: UsuariIdentificat) {
        val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        preferences.edit {
            putLong(KEY_USUARI_ID, usuari.id)
            putString(KEY_USUARI_DNI, usuari.dni)
            putString(KEY_USUARI_NOM, usuari.nomComplet)
            putString(KEY_USUARI_EMAIL, usuari.email)
            putString(KEY_USUARI_ROL, usuari.rolUsuari)
        }
    }


    /**
     * Obté l'usuari identificat.
     */
    fun obtenirUsuari(context: Context): UsuariIdentificat? {
        val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        if (!preferences.contains(KEY_USUARI_ID)) {
            return null
        }

        return UsuariIdentificat(
            id = preferences.getLong(KEY_USUARI_ID, 0L),
            dni = preferences.getString(KEY_USUARI_DNI, "") ?: "",
            nomComplet = preferences.getString(KEY_USUARI_NOM, "") ?: "",
            email = preferences.getString(KEY_USUARI_EMAIL, "") ?: "",
            rolUsuari = preferences.getString(KEY_USUARI_ROL, "") ?: ""
        )
    }


    /**
     * Indica si hi ha un usuari identificat.
     */
    fun hiHaUsuariIdentificat(context: Context): Boolean {
        val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return preferences.contains(KEY_USUARI_ID)
    }


    /**
     * Elimina les dades de l'usuari identificat.
     */
    fun tancarSessio(context: Context) {
        val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        preferences.edit {
            clear()
        }
    }
}