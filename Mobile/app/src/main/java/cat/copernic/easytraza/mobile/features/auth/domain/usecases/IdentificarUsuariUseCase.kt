package cat.copernic.easytraza.mobile.features.auth.domain.usecases

import android.content.Context
import cat.copernic.easytraza.mobile.core.network.RetrofitClient
import cat.copernic.easytraza.mobile.features.auth.data.sources.remote.AuthApi
import cat.copernic.easytraza.mobile.features.auth.data.sources.remote.IdentificarRequest
import cat.copernic.easytraza.mobile.features.auth.domain.models.UsuariIdentificat

/**
 * Cas d'ús per identificar un usuari sense contrasenya.
 */
class IdentificarUsuariUseCase(
    private val context: Context
) {

    /**
     * Obté l'API d'autenticació amb la IP configurada actualment.
     */
    private fun getAuthApi(): AuthApi {
        return RetrofitClient
            .getInstance(context)
            .create(AuthApi::class.java)
    }


    /**
     * Obté els usuaris disponibles per identificar-se.
     */
    suspend fun llistarUsuaris(): Result<List<UsuariIdentificat>> {

        return try {
            val response = getAuthApi().llistarUsuaris()

            if (response.isSuccessful) {
                val body = response.body()

                if (body != null) {
                    Result.success(
                        body.map { usuari ->
                            UsuariIdentificat(
                                id = usuari.id,
                                dni = usuari.dni,
                                nomComplet = usuari.nomComplet,
                                email = usuari.email,
                                rolUsuari = usuari.rolUsuari
                            )
                        }
                    )
                }
                else {
                    Result.failure(Exception("Resposta buida del servidor"))
                }
            }
            else {
                Result.failure(Exception("No s'han pogut carregar els usuaris"))
            }
        }
        catch (e: Exception) {
            Result.failure(Exception("No s'ha pogut connectar amb el servidor"))
        }
    }


    /**
     * Executa la identificació de l'usuari.
     */
    suspend fun executar(email: String): Result<UsuariIdentificat> {

        return try {
            val response = getAuthApi().identificar(
                IdentificarRequest(email.trim().lowercase())
            )

            if (response.isSuccessful) {
                val body = response.body()

                if (body != null) {
                    Result.success(
                        UsuariIdentificat(
                            id = body.id,
                            dni = body.dni,
                            nomComplet = body.nomComplet,
                            email = body.email,
                            rolUsuari = body.rolUsuari
                        )
                    )
                }
                else {
                    Result.failure(Exception("Resposta buida del servidor"))
                }
            }
            else {
                Result.failure(Exception("Usuari no trobat"))
            }
        }
        catch (e: Exception) {
            Result.failure(Exception("No s'ha pogut connectar amb el servidor"))
        }
    }
}