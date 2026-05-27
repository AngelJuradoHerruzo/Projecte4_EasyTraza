package cat.copernic.easytraza.mobile.core.config

import android.content.Context
import androidx.core.content.edit

object IpPreferences {

    private const val PREFS_NAME = "easytraza_config" // Nom del fitxer de preferències on es guardarà la IP
    private const val KEY_SERVER_IP = "server_ip" // Clau amb la qual es desa la IP del servidor
    private const val DEFAULT_SERVER_IP = "10.0.2.2" // Valor per defecte si encara no s'ha configurat cap IP
    private const val SERVER_PROTOCOL = "http"
    private const val SERVER_PORT = "8080"


    // Guarda la IP del servidor en memòria local persistent
    fun saveIp(context: Context, ip: String) {
        val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        preferences.edit { putString(KEY_SERVER_IP, formatAddress(ip)) }
    }

    // Recupera la URL completa del servidor per utilitzar-la com a baseUrl de Retrofit
    fun getIp(context: Context): String {
        return "$SERVER_PROTOCOL://${getAddress(context)}:$SERVER_PORT/"
    }

    // Recupera únicament l'adreça IPv4 que s'ha de mostrar al camp de configuració
    fun getAddress(context: Context): String {
        val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // Si no n'hi ha cap, retorna la de per defecte
        val savedAddress = preferences.getString(KEY_SERVER_IP, DEFAULT_SERVER_IP) ?: DEFAULT_SERVER_IP

        return formatAddress(savedAddress)
    }

    // Dona format correcte a l'adreça perquè l'usuari només hagi d'introduir la IPv4
    private fun formatAddress(ip: String): String {
        val address = ip.trim()
            .removePrefix("https://")
            .removePrefix("http://")
            .substringBefore("/")
            .substringBefore(":")

        return address.ifBlank { DEFAULT_SERVER_IP }
    }
}
