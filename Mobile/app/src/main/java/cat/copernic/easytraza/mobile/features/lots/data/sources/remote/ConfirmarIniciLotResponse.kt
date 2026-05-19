package cat.copernic.easytraza.mobile.features.lots.data.sources.remote

/**
 * DTO de resposta en iniciar un lot.
 */
data class ConfirmarIniciLotResponse(
    val requereixConfirmacio: Boolean,
    val missatge: String,
    val lot: LotResponse?
)