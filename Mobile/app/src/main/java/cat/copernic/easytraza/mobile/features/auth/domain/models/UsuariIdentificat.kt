package cat.copernic.easytraza.mobile.features.auth.domain.models

/**
 * USUARI IDENTIFICAT.
 *
 * Representades les dades mínimes de domini de l'usuari que manté
 * una sessió identificada dins de l'aplicació mòbil.
 *
 * @author Ángel Jurado Herruz
 */
data class UsuariIdentificat(
    val id: Long,
    val nomComplet: String
)