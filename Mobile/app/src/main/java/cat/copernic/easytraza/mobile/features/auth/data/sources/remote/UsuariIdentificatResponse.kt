package cat.copernic.easytraza.mobile.features.auth.data.sources.remote

/**
 * RESPOSTA D'USUARI IDENTIFICAT.
 *
 * Representades les dades mínimes retornades pel backend quan un usuari
 * ha estat identificat correctament des del client mòbil.
 *
 * @author Ángel Jurado Herruzo
 */
data class UsuariIdentificatResponse(
    val id: Long,
    val nomComplet: String
)