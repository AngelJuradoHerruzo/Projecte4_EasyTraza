package cat.copernic.easytraza.mobile.features.lots.domain.usecases

import android.content.Context
import cat.copernic.easytraza.mobile.R
import cat.copernic.easytraza.mobile.core.network.RetrofitClient
import cat.copernic.easytraza.mobile.features.lots.data.sources.remote.LotApi
import cat.copernic.easytraza.mobile.features.lots.data.sources.remote.LotResponse
import cat.copernic.easytraza.mobile.features.lots.domain.models.Lot

/**
 * RESULTAT D'INICI D'UN LOT.
 *
 * Representada la informació obtinguda en intentar iniciar un lot,
 * incloent-hi la confirmació requerida, el missatge i el lot resultant.
 *
 * @author Ángel Jurado Herruz
 */
class LotUseCase(
    private val context: Context
) {

    /**
     * OBTENCIÓ DE L'API DE LOTS.
     *
     * Preparat el servei remot de lots utilitzant
     * la configuració actual del servidor.
     *
     * @return API preparada per executar operacions de lots
     */
    private fun getLotApi(): LotApi {
        return RetrofitClient
            .getInstance(context)
            .create(LotApi::class.java)
    }


    /**
     * LLISTAT DE LOTS.
     *
     * Consultats els lots disponibles al backend i convertides
     * les respostes rebudes al model de domini.
     *
     * @return resultat amb els lots disponibles o l'error produït
     */
    suspend fun llistarLots(): Result<List<Lot>> {

        return try {
            val response = getLotApi().llistarLots()

            if (response.isSuccessful) {
                Result.success(
                    response.body()
                        ?.map { it.toDomain() }
                        ?: emptyList()
                )
            }
            else {
                Result.failure(Exception(context.getString(R.string.error_load_lots)))
            }
        }
        catch (e: Exception) {
            Result.failure(Exception(context.getString(R.string.error_server_connection)))
        }
    }


    /**
     * CONSULTA D'UN LOT.
     *
     * Consultades les dades del lot indicat i retornada
     * la seva representació de domini quan existeix.
     *
     * @param id identificador del lot que s'ha de consultar
     * @return resultat amb el lot consultat o l'error produït
     */
    suspend fun consultarLot(id: Long): Result<Lot> {

        return try {
            val response = getLotApi().consultarLot(id)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.toDomain())
            }
            else {
                Result.failure(Exception(context.getString(R.string.error_consult_lot)))
            }
        }
        catch (e: Exception) {
            Result.failure(Exception(context.getString(R.string.error_server_connection)))
        }
    }


    /**
     * INICI D'UN LOT.
     *
     * Sol·licitat l'inici del lot indicat i interpretada la resposta
     * per informar si és necessària una confirmació prèvia.
     *
     * @param id identificador del lot que s'ha d'iniciar
     * @return resultat amb la informació obtinguda en iniciar el lot
     */
    suspend fun iniciarLot(id: Long): Result<IniciarLotResult> {

        return try {
            val response = getLotApi().iniciarLot(id)

            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!

                Result.success(
                    IniciarLotResult(
                        requereixConfirmacio = body.requereixConfirmacio,
                        missatge = if (body.requereixConfirmacio) {
                            context.getString(R.string.lot_existing_open_message)
                        } else {
                            context.getString(R.string.lot_started_success)
                        },
                        lot = body.lot?.toDomain()
                    )
                )
            }
            else {
                Result.failure(Exception(context.getString(R.string.error_start_lot)))
            }
        }
        catch (e: Exception) {
            Result.failure(Exception(context.getString(R.string.error_server_connection)))
        }
    }


    /**
     * CONFIRMACIÓ D'INICI D'UN LOT.
     *
     * Executada la confirmació necessària per iniciar el lot indicat
     * i retornades les seves dades actualitzades.
     *
     * @param id identificador del lot que s'ha d'iniciar
     * @return resultat amb el lot iniciat o l'error produït
     */
    suspend fun confirmarIniciLot(id: Long): Result<Lot> {

        return try {
            val response = getLotApi().confirmarIniciLot(id)

            if (response.isSuccessful && response.body()?.lot != null) {
                Result.success(response.body()!!.lot!!.toDomain())
            }
            else {
                Result.failure(Exception(context.getString(R.string.error_start_lot)))
            }
        }
        catch (e: Exception) {
            Result.failure(Exception(context.getString(R.string.error_server_connection)))
        }
    }


    /**
     * FINALITZACIÓ D'UN LOT.
     *
     * Executada la finalització del lot indicat i retornades
     * les dades actualitzades després de completar l'operació.
     *
     * @param id identificador del lot que s'ha de finalitzar
     * @return resultat amb el lot finalitzat o l'error produït
     */
    suspend fun finalitzarLot(id: Long): Result<Lot> {

        return try {
            val response = getLotApi().finalitzarLot(id)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.toDomain())
            }
            else {
                Result.failure(Exception(context.getString(R.string.error_finish_lot)))
            }
        }
        catch (e: Exception) {
            Result.failure(Exception(context.getString(R.string.error_server_connection)))
        }
    }


    /**
     * CONVERSIÓ AL MODEL DE DOMINI.
     *
     * Transformada la resposta rebuda del backend en el model
     * de lot utilitzat per la resta de l'aplicació.
     *
     * @return lot convertit al model de domini
     */
    private fun LotResponse.toDomain(): Lot {
        return Lot(
            id = id,
            identificadorLot = identificadorLot,
            quantitat = quantitat,
            unitats = unitats,
            estat = estat,
            dataCaducitat = dataCaducitat ?: "",
            dataObertura = dataObertura ?: "",
            dataAcabament = dataAcabament ?: "",
            materiaPrimeraId = materiaPrimeraId,
            materiaPrimeraNom = materiaPrimeraNom,
            albaraProveidorId = albaraProveidorId
        )
    }
}


/**
 * Resultat d'iniciar un lot.
 */
data class IniciarLotResult(
    val requereixConfirmacio: Boolean,
    val missatge: String,
    val lot: Lot?
)
