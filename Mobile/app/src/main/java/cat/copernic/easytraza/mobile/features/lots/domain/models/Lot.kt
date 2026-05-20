package cat.copernic.easytraza.mobile.features.lots.domain.models

/**
 * Model de domini d'un lot.
 */
data class Lot(
    val id: Long,
    val identificadorLot: String,
    val quantitat: Int,
    val unitats: String,
    val estat: String,
    val dataCaducitat: String,
    val dataObertura: String,
    val dataAcabament: String,
    val materiaPrimeraId: Long?,
    val materiaPrimeraNom: String,
    val albaraProveidorId: Long?
)