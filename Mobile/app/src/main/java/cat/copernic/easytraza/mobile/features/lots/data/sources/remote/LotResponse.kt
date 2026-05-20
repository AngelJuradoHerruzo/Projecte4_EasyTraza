package cat.copernic.easytraza.mobile.features.lots.data.sources.remote

/**
 * DTO de resposta d'un lot.
 */
data class LotResponse(
    val id: Long,
    val identificadorLot: String,
    val quantitat: Int,
    val unitats: String,
    val estat: String,
    val dataCaducitat: String?,
    val dataObertura: String?,
    val dataAcabament: String?,
    val materiaPrimeraId: Long?,
    val materiaPrimeraNom: String,
    val albaraProveidorId: Long?
)