package cat.copernic.easytraza.mobile.features.auth.data.sources.remote

/**
 * PETICIÓ D'IDENTIFICACIÓ.
 *
 * Representat el correu electrònic enviat al backend per identificar
 * l'usuari seleccionat des de l'aplicació mòbil.
 *
 * @author Ángel Jurado Herruz
 */
data class IdentificarRequest(
    val email: String
)