package cat.copernic.easytraza.mobile.features.auth.data.sources.remote

/**
 * PETICIÓ D'IDENTIFICACIÓ.
 *
 * Representat l'identificador enviat al backend per identificar
 * l'usuari seleccionat des de l'aplicació mòbil.
 *
 * @author Ángel Jurado Herruzo
 */
data class IdentificarRequest(
    val id: Long
)