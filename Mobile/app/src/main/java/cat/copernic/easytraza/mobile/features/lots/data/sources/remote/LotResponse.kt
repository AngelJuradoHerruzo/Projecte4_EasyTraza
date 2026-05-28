package cat.copernic.easytraza.mobile.features.lots.data.sources.remote

/**
 * RESPOSTA D'UN LOT.
 *
 * Representades les dades d'un lot retornades pel backend
 * abans de transformar-les al model de domini de l'aplicació.
 *
 * @author Ángel Jurado Herruzo
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