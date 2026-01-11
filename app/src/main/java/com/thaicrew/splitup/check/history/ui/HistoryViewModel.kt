package com.thaicrew.splitup.history.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thaicrew.splitup.check.history.domain.GetPaidChecksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val getPaidChecksUseCase: GetPaidChecksUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryScreenState())
    val uiState: StateFlow<HistoryScreenState> = _uiState.asStateFlow()

    init {
        Timber.i("HistoryViewModel inicializada.")
        observeHistory()
    }

    private fun observeHistory() {
        getPaidChecksUseCase()
            .onEach { checks ->
                _uiState.update { it.copy(checks = checks, isLoading = false) }
            }
            .launchIn(viewModelScope)
    }
}