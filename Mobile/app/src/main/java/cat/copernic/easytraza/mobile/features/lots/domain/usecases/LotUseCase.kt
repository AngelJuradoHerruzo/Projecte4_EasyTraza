package cat.copernic.easytraza.mobile.features.lots.domain.usecases

import android.content.Context
import cat.copernic.easytraza.mobile.R
import cat.copernic.easytraza.mobile.core.network.RetrofitClient
import cat.copernic.easytraza.mobile.features.lots.data.sources.remote.LotApi
import cat.copernic.easytraza.mobile.features.lots.data.sources.remote.LotResponse
import cat.copernic.easytraza.mobile.features.lots.domain.models.Lot

/**
 * Casos d'ús de lots.
 */
class LotUseCase(
    private val context: Context
) {

    private fun getLotApi(): LotApi {
        return RetrofitClient
            .getInstance(context)
            .create(LotApi::class.java)
    }


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
