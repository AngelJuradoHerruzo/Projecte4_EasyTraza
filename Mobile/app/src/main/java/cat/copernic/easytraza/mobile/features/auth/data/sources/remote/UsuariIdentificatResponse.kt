package cat.copernic.easytraza.mobile.features.auth.data.sources.remote

/**
 * DTO de resposta de l'usuari identificat.
 */
data class UsuariIdentificatResponse(
    val id: Long,
    val dni: String,
    val nomComplet: String,
    val email: String,
    val rolUsuari: String
)