package cat.copernic.easytraza.mobile.core.network

import android.content.Context
import cat.copernic.easytraza.mobile.core.config.IpPreferences
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * CLIENT DE COMUNICACIÓ RETROFIT.
 *
 * Gestionada la instància de Retrofit utilitzada per comunicar l'aplicació
 * mòbil amb el backend segons l'adreça de servidor configurada.
 *
 * @author Ángel Jurado Herruz
 */
object RetrofitClient {

    private var retrofit: Retrofit? = null
    private var currentBaseUrl: String? = null


    /**
     * OBTENCIÓ DEL CLIENT RETROFIT.
     *
     * Obtinguda una instància de Retrofit preparada amb la URL configurada,
     * recreant-la quan l'adreça del servidor hagi estat modificada.
     *
     * @param context context utilitzat per obtenir la configuració del servidor
     * @return instància de Retrofit preparada per consumir l'API
     */
    fun getInstance(context: Context): Retrofit {

        val baseUrl = IpPreferences.getIp(context)

        if (retrofit == null || currentBaseUrl != baseUrl) {

            retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            currentBaseUrl = baseUrl
        }

        return retrofit!!
    }


    /**
     * REINICI DEL CLIENT RETROFIT.
     *
     * Eliminada la instància actual i la URL associada perquè la propera
     * petició construeixi el client amb la configuració vigent.
     */
    fun reset() {
        retrofit = null
        currentBaseUrl = null
    }
}
