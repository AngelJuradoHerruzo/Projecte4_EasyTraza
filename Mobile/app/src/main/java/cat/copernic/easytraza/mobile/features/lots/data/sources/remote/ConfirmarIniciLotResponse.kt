package cat.copernic.easytraza.mobile.features.lots.data.sources.remote

/**
 * RESPOSTA D'INICI DE LOT.
 *
 * Representat el resultat retornat pel backend en iniciar un lot,
 * indicant si cal confirmar l'operació i el lot afectat.
 *
 * @author Ángel Jurado Herruzo
 */
data class ConfirmarIniciLotResponse(
    val requereixConfirmacio: Boolean,
    val missatge: String,
    val lot: LotResponse?
)