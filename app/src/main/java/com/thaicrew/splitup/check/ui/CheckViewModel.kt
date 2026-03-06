package com.thaicrew.splitup.check.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thaicrew.splitup.check.domain.Check
import com.thaicrew.splitup.check.domain.CheckHasItemsUseCase
import com.thaicrew.splitup.check.domain.CloseCheckUseCase
import com.thaicrew.splitup.check.domain.CreateCheckUseCase
import com.thaicrew.splitup.check.domain.DeleteCheckUseCase
import com.thaicrew.splitup.check.domain.GetOpenChecksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class CheckViewModel @Inject constructor(
    private val getOpenChecksUseCase: GetOpenChecksUseCase,
    private val createCheckUseCase: CreateCheckUseCase,
    private val deleteCheckUseCase: DeleteCheckUseCase,
    private val closeCheckUseCase: CloseCheckUseCase,
    private val checkHasItemsUseCase: CheckHasItemsUseCase

) : ViewModel() {

    private val _uiState = MutableStateFlow(CheckScreenState())
    val uiState: StateFlow<CheckScreenState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<CheckUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    private val _confirmationState = MutableStateFlow<Pair<Check, SwipeAction>?>(null)
    val confirmationState: StateFlow<Pair<Check, SwipeAction>?> = _confirmationState.asStateFlow()

    enum class SwipeAction { DELETE, CLOSE }

    init {
        Timber.i("CheckViewModel inicializada.")
        observeChecks()
    }

    private fun observeChecks() {
        Timber.d("Iniciando observação de comandas abertas.")
        getOpenChecksUseCase()
            .onEach { checks ->
                _uiState.update { it.copy(checks = checks, isLoading = false) }
            }
            .launchIn(viewModelScope)
    }

    fun onSwipeAction(check: Check, action: SwipeAction) {
        when (action) {
            SwipeAction.DELETE -> {
                _confirmationState.value = check to action
            }
            SwipeAction.CLOSE -> {
                viewModelScope.launch {
                    val hasItems = checkHasItemsUseCase(check.id)
                    if (hasItems) {
                        _confirmationState.value = check to action
                    } else {
                        _uiState.update {
                            it.copy(infoDialogMessage = "Não é possível fechar a comanda '${check.name}' porque ela não possui nenhum item adicionado.")
                        }
                    }
                }
            }
        }
    }

    fun onDismissInfoDialog() {
        _uiState.update { it.copy(infoDialogMessage = null) }
    }

    fun onDismissDialog() {
        _confirmationState.value = null
    }

    // Chamado ao confirmar o Dialog
    fun onConfirmAction() {
        val (check, action) = _confirmationState.value ?: return
        viewModelScope.launch {
            try {
                when (action) {
                    SwipeAction.DELETE -> {
                        Timber.i("Deletando comanda: ${check.id}")
                        deleteCheckUseCase(check)
                    }
                    SwipeAction.CLOSE -> {
                        Timber.i("Fechando comanda: ${check.id}")
                        closeCheckUseCase(check)
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Erro ao executar ação na comanda.")
            } finally {
                _confirmationState.value = null
            }
        }
    }

    fun onCreateCheckConfirmed(name: String) {
        val trimmed = name.trim()
        if (trimmed.isBlank()) {
            return
        }

        viewModelScope.launch {
            try {
                Timber.i("Criando nova comanda com nome: $trimmed")
                val newCheckId = createCheckUseCase(trimmed)
                Timber.d("Comanda criada com ID: $newCheckId. Navegando para detalhes.")
                _uiEvent.emit(CheckUiEvent.NavigateToCheckDetail(newCheckId.toInt()))
            } catch (e: Exception) {
                Timber.e(e, "Erro ao criar nova comanda.")
            }
        }
    }
    fun onCheckClicked(checkId: Int) {
        viewModelScope.launch {
            Timber.i("Comanda ID $checkId selecionada. Navegando para detalhes.")
            _uiEvent.emit(CheckUiEvent.NavigateToCheckDetail(checkId))
        }
    }

}