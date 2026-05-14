package cat.copernic.easytraza.mobile.features.auth.domain.models

/**
 * Model de domini de l'usuari identificat.
 */
data class UsuariIdentificat(
    val id: Long,
    val dni: String,
    val nomComplet: String,
    val email: String,
    val rolUsuari: String
)