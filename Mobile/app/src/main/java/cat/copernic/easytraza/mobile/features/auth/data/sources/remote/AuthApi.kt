package cat.copernic.easytraza.mobile.features.auth.data.sources.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * Endpoints REST d'autenticació mòbil.
 */
interface AuthApi {

    /**
     * Obté els usuaris disponibles per identificar-se.
     */
    @GET("api/mobile/auth/usuaris")
    suspend fun llistarUsuaris(): Response<List<UsuariIdentificatResponse>>


    /**
     * Identifica un usuari sense contrasenya.
     */
    @POST("api/mobile/auth/identificar")
    suspend fun identificar(
        @Body request: IdentificarRequest
    ): Response<UsuariIdentificatResponse>
}