package cat.copernic.easytraza.mobile.core.config

import android.content.Context
import androidx.core.content.edit

object IpPreferences {

    private const val PREFS_NAME = "easytraza_config" // Nom del fitxer de preferències on es guardarà la IP
    private const val KEY_SERVER_IP = "server_ip" // Clau amb la qual es desa la IP del servidor
    private const val DEFAULT_SERVER_IP = "http://10.0.2.2:8080/" // Valor per defecte si encara no s'ha configurat cap IP


    // Guarda la IP del servidor en memòria local persistent
    fun saveIp(context: Context, ip: String) {
        val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        preferences.edit { putString(KEY_SERVER_IP, formatIp(ip)) }
    }

    // Recupera la IP guardada
    fun getIp(context: Context): String {
        val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // Si no n'hi ha cap, retorna la de per defecte
        return preferences.getString(KEY_SERVER_IP, DEFAULT_SERVER_IP) ?: DEFAULT_SERVER_IP
    }

    // Dona format correcte a la IP perquè Retrofit la pugui utilitzar com a baseUrl
    private fun formatIp(ip: String): String {
        var url = ip.trim()

        // Si l'usuari no ha escrit http:// o https://, s'afegeix automàticament
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "http://$url"
        }

        // Retrofit necessita que la URL acabi amb /
        if (!url.endsWith("/")) {
            url += "/"
        }

        return url
    }
}