package cat.copernic.easytraza.mobile.features.lots.domain.models

/**
 * LOT DE MATÈRIA PRIMERA.
 *
 * Representades les dades de domini d'un lot que l'operari
 * pot consultar, iniciar o finalitzar des de l'aplicació mòbil.
 *
 * @author Ángel Jurado Herruzo
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