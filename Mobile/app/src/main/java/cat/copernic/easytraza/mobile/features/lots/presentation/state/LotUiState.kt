package cat.copernic.easytraza.mobile.features.lots.presentation.state

import cat.copernic.easytraza.mobile.features.lots.domain.models.Lot

/**
 * ESTAT DE LA GESTIÓ DE LOTS.
 *
 * Representada la informació visible a les pantalles de lots,
 * incloent-hi el llistat, el detall, els missatges i les confirmacions.
 *
 * @author Ángel Jurado Herruz
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