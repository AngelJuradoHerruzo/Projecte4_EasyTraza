package cat.copernic.easytraza.mobile.core.network

import android.content.Context
import cat.copernic.easytraza.mobile.core.config.IpPreferences
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    fun getInstance(context: Context): Retrofit {

        // Obté la IP guardada a les preferències
        val baseUrl = IpPreferences.getIp(context)

        // Crea la instància de Retrofit amb la IP configurada
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}