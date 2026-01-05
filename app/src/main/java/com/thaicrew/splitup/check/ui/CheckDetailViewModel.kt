package com.thaicrew.splitup.check.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thaicrew.splitup.check.domain.GetCheckByIdFlowUseCase
import com.thaicrew.splitup.check.domain.UpdateCheckResult
import com.thaicrew.splitup.check.domain.UpdateNameCheckUseCase
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
class CheckDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getCheckByIdFlowUseCase: GetCheckByIdFlowUseCase,
    private val updateCheckUseCase: UpdateNameCheckUseCase
) : ViewModel() {

    private val checkId: Int = checkNotNull(savedStateHandle["checkId"])

    private val _uiState = MutableStateFlow(CheckDetailScreenState())
    val uiState: StateFlow<CheckDetailScreenState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<CheckDetailUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    init {
        Timber.i("CheckDetailViewModel iniciada para a comanda ID: $checkId")
        observeCheck()
    }

    private fun observeCheck() {
        getCheckByIdFlowUseCase(checkId)
            .onEach { check ->
                if (check == null) {
                    Timber.w("Comanda ID $checkId não encontrada.")
                    _uiState.update { it.copy(isLoading = false, isError = true) }
                } else {
                    Timber.d("Dados da comanda ID $checkId carregados: ${check.name}")
                    _uiState.update { it.copy(check = check, isLoading = false) }
                }
            }
            .launchIn(viewModelScope)
    }

    fun onNameChanged(newName: String) {
        val currentCheck = uiState.value.check ?: return

        viewModelScope.launch {
            when (val result = updateCheckUseCase(currentCheck, newName)) {
                is UpdateCheckResult.Success -> {
                    Timber.i("Nome da comanda atualizado com sucesso para: '$newName'")
                }
                is UpdateCheckResult.Error -> {
                    Timber.e("Erro ao atualizar nome: ${result.message}")
                    _uiEvent.emit(CheckDetailUiEvent.ShowSnackbar(result.message))
                }
            }
        }
    }
}