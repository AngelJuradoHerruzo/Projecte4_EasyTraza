package cat.copernic.easytraza.mobile.features.auth.domain.usecases

import android.content.Context
import cat.copernic.easytraza.mobile.R
import cat.copernic.easytraza.mobile.core.network.RetrofitClient
import cat.copernic.easytraza.mobile.features.auth.data.sources.remote.AuthApi
import cat.copernic.easytraza.mobile.features.auth.data.sources.remote.IdentificarRequest
import cat.copernic.easytraza.mobile.features.auth.domain.models.UsuariIdentificat

/**
 * EXECUCIÓ DE LA IDENTIFICACIÓ.
 *
 * Enviat el correu electrònic seleccionat al backend i retornades
 * les dades de l'usuari quan la identificació es completa correctament.
 *
 * @param email correu electrònic de l'usuari que s'ha d'identificar
 * @return resultat amb l'usuari identificat o l'error produït
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
                    Result.failure(Exception(context.getString(R.string.error_empty_response)))
                }
            }
            else {
                Result.failure(Exception(context.getString(R.string.error_load_users)))
            }
        }
        catch (e: Exception) {
            Result.failure(Exception(context.getString(R.string.error_server_connection)))
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
                    Result.failure(Exception(context.getString(R.string.error_empty_response)))
                }
            }
            else {
                Result.failure(Exception(context.getString(R.string.error_user_not_found)))
            }
        }
        catch (e: Exception) {
            Result.failure(Exception(context.getString(R.string.error_server_connection)))
        }
    }
}
