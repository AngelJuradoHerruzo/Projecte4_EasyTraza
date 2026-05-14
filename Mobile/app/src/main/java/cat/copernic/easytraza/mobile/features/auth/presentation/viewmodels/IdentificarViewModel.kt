package cat.copernic.easytraza.mobile.features.auth.presentation.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cat.copernic.easytraza.mobile.core.session.UsuariSessionManager
import cat.copernic.easytraza.mobile.features.auth.domain.models.UsuariIdentificat
import cat.copernic.easytraza.mobile.features.auth.domain.usecases.IdentificarUsuariUseCase
import cat.copernic.easytraza.mobile.features.auth.presentation.state.IdentificarUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel de la pantalla d'identificació.
 */
class IdentificarViewModel(
    private val context: Context
) : ViewModel() {

    private val identificarUsuariUseCase = IdentificarUsuariUseCase(context)

    private val _uiState = MutableStateFlow(IdentificarUiState())
    val uiState: StateFlow<IdentificarUiState> = _uiState.asStateFlow()


    init {
        carregarUsuaris()
    }


    /**
     * Carrega els usuaris disponibles.
     */
    fun carregarUsuaris() {

        _uiState.value = _uiState.value.copy(
            carregant = true,
            error = null
        )

        viewModelScope.launch {
            val result = identificarUsuariUseCase.llistarUsuaris()

            result
                .onSuccess { usuaris ->
                    _uiState.value = _uiState.value.copy(
                        usuaris = usuaris,
                        carregant = false,
                        error = null
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        carregant = false,
                        error = exception.message
                    )
                }
        }
    }


    /**
     * Identifica l'usuari seleccionat.
     */
    fun identificarUsuari(
        usuari: UsuariIdentificat,
        onSuccess: () -> Unit
    ) {

        _uiState.value = _uiState.value.copy(
            carregant = true,
            error = null
        )

        viewModelScope.launch {
            val result = identificarUsuariUseCase.executar(usuari.email)

            result
                .onSuccess { usuariIdentificat ->
                    UsuariSessionManager.guardarUsuari(context, usuariIdentificat)

                    _uiState.value = _uiState.value.copy(
                        carregant = false,
                        error = null
                    )

                    onSuccess()
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        carregant = false,
                        error = exception.message
                    )
                }
        }
    }
}