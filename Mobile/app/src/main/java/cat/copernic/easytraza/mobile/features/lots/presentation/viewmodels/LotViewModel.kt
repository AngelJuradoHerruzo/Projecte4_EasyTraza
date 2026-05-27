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
 * VIEWMODEL DE LOTS.
 *
 * Gestionat l'estat de les pantalles de lots i coordinades
 * les operacions de consulta, inici, confirmació i finalització.
 *
 * @author Ángel Jurado Herruz
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


    /**
     * CÀRREGA DE LOTS.
     *
     * Sol·licitats els lots disponibles i actualitzat l'estat
     * de la pantalla segons el resultat obtingut.
     */
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


    /**
     * CONSULTA D'UN LOT.
     *
     * Sol·licitades les dades del lot indicat i actualitzat
     * el detall seleccionat dins de l'estat de la pantalla.
     *
     * @param id identificador del lot que s'ha de consultar
     */
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


    /**
     * INICI D'UN LOT.
     *
     * Executada la petició d'inici del lot i mostrada
     * la confirmació necessària quan existeix un lot anterior obert.
     *
     * @param lot lot que s'ha d'iniciar
     */
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


    /**
     * CONFIRMACIÓ D'INICI DEL LOT.
     *
     * Confirmat l'inici del lot pendent i actualitzat
     * l'estat visible quan l'operació finalitza correctament.
     */
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


    /**
     * CANCEL·LACIÓ DE LA CONFIRMACIÓ.
     *
     * Tancada la confirmació d'inici del lot i eliminada
     * la selecció pendent de confirmar.
     */
    fun cancelLarConfirmacioInici() {
        _uiState.value = _uiState.value.copy(
            mostrarConfirmacioInici = false,
            lotPendentConfirmacio = null
        )
    }


    /**
     * FINALITZACIÓ D'UN LOT.
     *
     * Executada la finalització del lot seleccionat i actualitzades
     * les dades visibles després de completar l'operació.
     *
     * @param lot lot que s'ha de finalitzar
     */
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


    /**
     * NETEJA DE MISSATGES.
     *
     * Eliminats els missatges d'error o confirmació
     * mostrats actualment a la pantalla de lots.
     */
    fun netejarMissatges() {
        _uiState.value = _uiState.value.copy(
            error = null,
            missatge = null
        )
    }
}