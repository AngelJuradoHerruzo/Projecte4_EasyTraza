package cat.copernic.easytraza.mobile.core.session

import android.content.Context
import androidx.core.content.edit
import cat.copernic.easytraza.mobile.features.auth.domain.models.UsuariIdentificat

/**
 * GESTIÓ DE LA SESSIÓ LOCAL.
 *
 * Gestionades les dades locals de l'usuari identificat per mantenir
 * la seva sessió disponible durant l'ús de l'aplicació mòbil.
 *
 * @author Ángel Jurado Herruzo
 */
object UsuariSessionManager {

    private const val PREFS_NAME = "easytraza_session"
    private const val KEY_USUARI_ID = "usuari_id"
    private const val KEY_USUARI_NOM = "usuari_nom"


    /**
     * GUARDAT DE L'USUARI IDENTIFICAT.
     *
     * Desades a les preferències locals les dades de l'usuari
     * que ha estat identificat correctament a l'aplicació.
     *
     * @param context context utilitzat per accedir a les preferències locals
     * @param usuari usuari identificat que s'ha de conservar
     */
    fun guardarUsuari(context: Context, usuari: UsuariIdentificat) {
        val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        preferences.edit {
            putLong(KEY_USUARI_ID, usuari.id)
            putString(KEY_USUARI_NOM, usuari.nomComplet)
        }
    }


    /**
     * OBTENCIÓ DE L'USUARI IDENTIFICAT.
     *
     * Recuperades les dades de l'usuari desades localment o retornat
     * un valor nul quan encara no existeix cap sessió identificada.
     *
     * @param context context utilitzat per accedir a les preferències locals
     * @return usuari identificat desat o nul si no hi ha cap sessió
     */
    fun obtenirUsuari(context: Context): UsuariIdentificat? {
        val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        if (!preferences.contains(KEY_USUARI_ID)) {
            return null
        }

        return UsuariIdentificat(
            id = preferences.getLong(KEY_USUARI_ID, 0L),
            nomComplet = preferences.getString(KEY_USUARI_NOM, "") ?: ""
        )
    }


    /**
     * COMPROVACIÓ DE LA SESSIÓ.
     *
     * Comprovat si existeixen dades locals corresponents
     * a un usuari identificat a l'aplicació.
     *
     * @param context context utilitzat per accedir a les preferències locals
     * @return cert si hi ha un usuari identificat desat
     */
    fun hiHaUsuariIdentificat(context: Context): Boolean {
        val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return preferences.contains(KEY_USUARI_ID)
    }


    /**
     * TANCAMENT DE LA SESSIÓ.
     *
     * Eliminades les dades locals de l'usuari identificat
     * per finalitzar la sessió mantinguda al dispositiu.
     *
     * @param context context utilitzat per accedir a les preferències locals
     */
    fun tancarSessio(context: Context) {
        val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        preferences.edit {
            clear()
        }
    }
}
