package cat.copernic.easytraza.mobile.features.lots.presentation.viewmodels

import android.content.Context
import cat.copernic.easytraza.mobile.R
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cat.copernic.easytraza.mobile.features.lots.domain.models.Lot
import cat.copernic.easytraza.mobile.features.lots.domain.usecases.LotUseCase
import cat.copernic.easytraza.mobile.features.lots.presentation.state.LotUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel de lots.
 */
class LotViewModel(
    private val context: Context
) : ViewModel() {

    private val lotUseCase = LotUseCase(context)

    private val _uiState = MutableStateFlow(LotUiState())
    val uiState: StateFlow<LotUiState> = _uiState.asStateFlow()


    init {
        carregarLots()
    }


    fun carregarLots() {

        _uiState.value = _uiState.value.copy(
            carregant = true,
            error = null,
            missatge = null
        )

        viewModelScope.launch {
            val result = lotUseCase.llistarLots()

            result
                .onSuccess { lots ->
                    _uiState.value = _uiState.value.copy(
                        lots = lots,
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


    fun consultarLot(id: Long) {

        _uiState.value = _uiState.value.copy(
            carregant = true,
            error = null,
            missatge = null
        )

        viewModelScope.launch {
            val result = lotUseCase.consultarLot(id)

            result
                .onSuccess { lot ->
                    _uiState.value = _uiState.value.copy(
                        lotSeleccionat = lot,
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


    fun iniciarLot(lot: Lot) {

        _uiState.value = _uiState.value.copy(
            carregant = true,
            error = null,
            missatge = null
        )

        viewModelScope.launch {
            val result = lotUseCase.iniciarLot(lot.id)

            result
                .onSuccess { iniciarResult ->
                    if (iniciarResult.requereixConfirmacio) {
                        _uiState.value = _uiState.value.copy(
                            carregant = false,
                            mostrarConfirmacioInici = true,
                            lotPendentConfirmacio = lot,
                            missatge = iniciarResult.missatge
                        )
                    }
                    else {
                        _uiState.value = _uiState.value.copy(
                            carregant = false,
                            missatge = iniciarResult.missatge
                        )

                        carregarLots()
                        consultarLot(lot.id)
                    }
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        carregant = false,
                        error = exception.message
                    )
                }
        }
    }


    fun confirmarIniciLot() {

        val lot = _uiState.value.lotPendentConfirmacio ?: return

        _uiState.value = _uiState.value.copy(
            carregant = true,
            error = null
        )

        viewModelScope.launch {
            val result = lotUseCase.confirmarIniciLot(lot.id)

            result
                .onSuccess { lotIniciat ->
                    _uiState.value = _uiState.value.copy(
                        lotSeleccionat = lotIniciat,
                        carregant = false,
                        mostrarConfirmacioInici = false,
                        lotPendentConfirmacio = null,
                        missatge = context.getString(R.string.lot_started_success)
                    )

                    carregarLots()
                    consultarLot(lot.id)
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        carregant = false,
                        error = exception.message
                    )
                }
        }
    }


    fun cancelLarConfirmacioInici() {
        _uiState.value = _uiState.value.copy(
            mostrarConfirmacioInici = false,
            lotPendentConfirmacio = null
        )
    }


    fun finalitzarLot(lot: Lot) {

        _uiState.value = _uiState.value.copy(
            carregant = true,
            error = null,
            missatge = null
        )

        viewModelScope.launch {
            val result = lotUseCase.finalitzarLot(lot.id)

            result
                .onSuccess { lotFinalitzat ->
                    _uiState.value = _uiState.value.copy(
                        lotSeleccionat = lotFinalitzat,
                        carregant = false,
                        missatge = context.getString(R.string.lot_finished_success)
                    )

                    carregarLots()
                    consultarLot(lot.id)
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        carregant = false,
                        error = exception.message
                    )
                }
        }
    }


    fun netejarMissatges() {
        _uiState.value = _uiState.value.copy(
            error = null,
            missatge = null
        )
    }
}