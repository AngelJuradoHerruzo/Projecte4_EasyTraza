package cat.copernic.easytraza.mobile.features.lots.data.sources.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * API REMOTA DE LOTS.
 *
 * Definits els endpoints REST utilitzats per consultar els lots
 * i executar-ne les operacions d'inici i finalització.
 *
 * @author Ángel Jurado Herruzo
 */
interface LotApi {

    /**
     * LLISTAT DE LOTS.
     *
     * Obtinguts del backend tots els lots disponibles per mostrar-los
     * a la pantalla principal de gestió de lots.
     *
     * @return resposta HTTP amb la llista de lots
     */
    @GET("api/mobile/lots")
    suspend fun llistarLots(): Response<List<LotResponse>>


    /**
     * CONSULTA D'UN LOT.
     *
     * Obtingudes del backend les dades completes del lot indicat.
     *
     * @param id identificador del lot que s'ha de consultar
     * @return resposta HTTP amb les dades del lot
     */
    @GET("api/mobile/lots/{id}")
    suspend fun consultarLot(
        @Path("id") id: Long
    ): Response<LotResponse>

    
    /**
     * INICI D'UN LOT.
     *
     * Sol·licitat l'inici del lot indicat i rebuda la informació
     * necessària per confirmar l'operació quan correspongui.
     *
     * @param id identificador del lot que s'ha d'iniciar
     * @return resposta HTTP amb el resultat de la petició d'inici
     */
    @POST("api/mobile/lots/{id}/iniciar")
    suspend fun iniciarLot(
        @Path("id") id: Long
    ): Response<ConfirmarIniciLotResponse>

    
    /**
     * CONFIRMACIÓ D'INICI D'UN LOT.
     *
     * Confirmat l'inici del lot indicat quan existeix
     * un lot anterior que ha de finalitzar-se.
     *
     * @param id identificador del lot que s'ha d'iniciar
     * @return resposta HTTP amb el resultat de la confirmació
     */
    @POST("api/mobile/lots/{id}/iniciar-confirmat")
    suspend fun confirmarIniciLot(
        @Path("id") id: Long
    ): Response<ConfirmarIniciLotResponse>

    
    /**
     * FINALITZACIÓ D'UN LOT.
     *
     * Sol·licitada al backend la finalització del lot indicat.
     *
     * @param id identificador del lot que s'ha de finalitzar
     * @return resposta HTTP amb les dades actualitzades del lot
     */
    @POST("api/mobile/lots/{id}/finalitzar")
    suspend fun finalitzarLot(
        @Path("id") id: Long
    ): Response<LotResponse>
}