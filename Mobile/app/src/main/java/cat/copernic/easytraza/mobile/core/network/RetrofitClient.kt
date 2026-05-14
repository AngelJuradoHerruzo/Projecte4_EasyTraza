package cat.copernic.easytraza.mobile.core.network

import android.content.Context
import cat.copernic.easytraza.mobile.core.config.IpPreferences
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Client Retrofit per comunicar l'app mòbil amb el backend.
 */
object RetrofitClient {

    private var retrofit: Retrofit? = null
    private var currentBaseUrl: String? = null


    /**
     * Obté una instància de Retrofit amb la IP configurada.
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
     * Reinicia la instància de Retrofit.
     */
    fun reset() {
        retrofit = null
        currentBaseUrl = null
    }
}