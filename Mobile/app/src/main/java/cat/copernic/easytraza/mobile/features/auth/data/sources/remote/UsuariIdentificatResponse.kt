package cat.copernic.easytraza.mobile.features.auth.data.sources.remote

/**
 * RESPOSTA D'USUARI IDENTIFICAT.
 *
 * Representades les dades retornades pel backend quan un usuari
 * ha estat identificat correctament des del client mòbil.
 *
 * @author Ángel Jurado Herruz
 */
data class UsuariIdentificatResponse(
    val id: Long,
    val dni: String,
    val nomComplet: String,
    val email: String,
    val rolUsuari: String
)