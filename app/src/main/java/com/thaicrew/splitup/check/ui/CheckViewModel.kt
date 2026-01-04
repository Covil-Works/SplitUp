package com.thaicrew.splitup.check.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private val getOpenChecksUseCase: GetOpenChecksUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CheckScreenState())
    val uiState: StateFlow<CheckScreenState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<CheckUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

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

    fun onAddCheckClicked() {
        viewModelScope.launch {
            Timber.i("Botão de adicionar comanda clicado. Disparando evento de navegação.")
            _uiEvent.emit(CheckUiEvent.NavigateToCreateCheck)
        }
    }
}