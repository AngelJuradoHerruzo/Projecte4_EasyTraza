package cat.copernic.easytraza.mobile.features.lots.data.sources.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Endpoints REST de lots.
 */
interface LotApi {

    @GET("api/mobile/lots")
    suspend fun llistarLots(): Response<List<LotResponse>>

    @GET("api/mobile/lots/{id}")
    suspend fun consultarLot(
        @Path("id") id: Long
    ): Response<LotResponse>

    @POST("api/mobile/lots/{id}/iniciar")
    suspend fun iniciarLot(
        @Path("id") id: Long
    ): Response<ConfirmarIniciLotResponse>

    @POST("api/mobile/lots/{id}/iniciar-confirmat")
    suspend fun confirmarIniciLot(
        @Path("id") id: Long
    ): Response<ConfirmarIniciLotResponse>

    @POST("api/mobile/lots/{id}/finalitzar")
    suspend fun finalitzarLot(
        @Path("id") id: Long
    ): Response<LotResponse>
}