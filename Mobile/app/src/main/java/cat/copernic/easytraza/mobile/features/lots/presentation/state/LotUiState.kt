package cat.copernic.easytraza.mobile.features.lots.presentation.state

import cat.copernic.easytraza.mobile.features.lots.domain.models.Lot

/**
 * Estat de les pantalles de lots.
 */
data class LotUiState(
    val lots: List<Lot> = emptyList(),
    val lotSeleccionat: Lot? = null,
    val carregant: Boolean = false,
    val error: String? = null,
    val missatge: String? = null,
    val mostrarConfirmacioInici: Boolean = false,
    val lotPendentConfirmacio: Lot? = null
)