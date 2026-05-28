package cat.copernic.easytraza.mobile.features.auth.presentation.state

import cat.copernic.easytraza.mobile.features.auth.domain.models.UsuariIdentificat

/**
 * ESTAT DE LA IDENTIFICACIÓ.
 *
 * Representada la informació visible de la pantalla d'identificació,
 * incloent-hi els usuaris disponibles, la càrrega i els possibles errors.
 *
 * @author Ángel Jurado Herruzo
 */
data class IdentificarUiState(
    val usuaris: List<UsuariIdentificat> = emptyList(),
    val carregant: Boolean = false,
    val error: String? = null
)