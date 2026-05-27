package cat.copernic.easytraza.mobile.core.config

import android.content.Context
import androidx.core.content.edit

/**
 * CONFIGURACIÓ DE L'ADREÇA DEL SERVIDOR.
 *
 * Gestionada la persistència local de l'adreça IPv4 utilitzada per l'aplicació
 * mòbil i construïda la URL necessària per comunicar-se amb el backend.
 *
 * @author Ángel Jurado Herruz
 */
object IpPreferences {

    private const val PREFS_NAME = "easytraza_config" // Nom del fitxer de preferències on es guardarà la IP
    private const val KEY_SERVER_IP = "server_ip" // Clau amb la qual es desa la IP del servidor
    private const val DEFAULT_SERVER_IP = "10.0.2.2" // Valor per defecte si encara no s'ha configurat cap IP
    private const val SERVER_PROTOCOL = "http"
    private const val SERVER_PORT = "8080"


    /**
     * GUARDAT DE L'ADREÇA DEL SERVIDOR.
     *
     * Desada de forma persistent l'adreça IPv4 indicada després
     * d'aplicar-hi el format admès per la configuració mòbil.
     *
     * @param context context utilitzat per accedir a les preferències locals
     * @param ip adreça IPv4 del servidor que s'ha de desar
     */
    fun saveIp(context: Context, ip: String) {
        val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        preferences.edit { putString(KEY_SERVER_IP, formatAddress(ip)) }
    }


    /**
     * OBTENCIÓ DE LA URL DEL SERVIDOR.
     *
     * Construïda la URL completa del backend a partir de l'adreça
     * desada, el protocol i el port configurats per a Mobile.
     *
     * @param context context utilitzat per accedir a les preferències locals
     * @return URL base preparada per realitzar les peticions de xarxa
     */
    fun getIp(context: Context): String {
        return "$SERVER_PROTOCOL://${getAddress(context)}:$SERVER_PORT/"
    }


    /**
     * OBTENCIÓ DE L'ADREÇA IPv4.
     *
     * Recuperada l'adreça IPv4 desada per mostrar-la o utilitzar-la
     * a la configuració, aplicant el valor per defecte quan sigui necessari.
     *
     * @param context context utilitzat per accedir a les preferències locals
     * @return adreça IPv4 configurada per al servidor
     */
    fun getAddress(context: Context): String {
        val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // Si no n'hi ha cap, retorna la de per defecte
        val savedAddress = preferences.getString(KEY_SERVER_IP, DEFAULT_SERVER_IP) ?: DEFAULT_SERVER_IP

        return formatAddress(savedAddress)
    }


    /**
     * NORMALITZACIÓ DE L'ADREÇA.
     *
     * Netejada l'adreça introduïda per conservar únicament la IPv4
     * i retornat el valor predeterminat quan l'entrada queda buida.
     *
     * @param ip adreça introduïda que s'ha de normalitzar
     * @return adreça IPv4 preparada per ser desada
     */
    private fun formatAddress(ip: String): String {
        val address = ip.trim()
            .removePrefix("https://")
            .removePrefix("http://")
            .substringBefore("/")
            .substringBefore(":")

        return address.ifBlank { DEFAULT_SERVER_IP }
    }
}
