package cat.copernic.easytraza.mobile.features.auth.presentation.state

import cat.copernic.easytraza.mobile.features.auth.domain.models.UsuariIdentificat

/**
 * Estat de la pantalla d'identificació.
 */
data class IdentificarUiState(
    val usuaris: List<UsuariIdentificat> = emptyList(),
    val carregant: Boolean = false,
    val error: String? = null
)