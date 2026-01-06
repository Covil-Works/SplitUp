package com.thaicrew.splitup.check.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thaicrew.splitup.check.domain.AddParticipantsUseCase
import com.thaicrew.splitup.check.domain.GetCheckByIdFlowUseCase
import com.thaicrew.splitup.check.domain.UpdateCheckResult
import com.thaicrew.splitup.check.domain.UpdateNameCheckUseCase
import com.thaicrew.splitup.check.domain.GetParticipantsUseCase
import com.thaicrew.splitup.check.domain.RemoveParticipantUseCase
import com.thaicrew.splitup.friend.domain.GetActiveFriendsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
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
    private val updateCheckUseCase: UpdateNameCheckUseCase,
    private val getParticipantsUseCase: GetParticipantsUseCase,
    private val addParticipantsUseCase: AddParticipantsUseCase,
    private val removeParticipantUseCase: RemoveParticipantUseCase,
    private val getActiveFriendsUseCase: GetActiveFriendsUseCase
) : ViewModel() {

    private val checkId: Int = checkNotNull(savedStateHandle["checkId"])

    private val _uiState = MutableStateFlow(CheckDetailScreenState())
    val uiState: StateFlow<CheckDetailScreenState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<CheckDetailUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    init {
        Timber.i("CheckDetailViewModel iniciada para a comanda ID: $checkId")
        observeCheck()
        observeParticipants()
    }

    private fun observeCheck() {
        getCheckByIdFlowUseCase(checkId)
            .onEach { check ->
                if (check == null) {
                    _uiState.update { it.copy(isLoading = false, isError = true) }
                } else {
                    _uiState.update { it.copy(check = check, isLoading = false) }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun observeParticipants() {
        getParticipantsUseCase(checkId)
            .onEach { list ->
                _uiState.update { it.copy(participants = list) }
            }.launchIn(viewModelScope)
    }

    fun onDismissBottomSheet() {
        _uiState.update { it.copy(showBottomSheet = false) }
    }

    fun onAddFriendsClicked() {
        viewModelScope.launch {
            try {
                val allFriends = getActiveFriendsUseCase().first()

                val participantIds = uiState.value.participants.map { it.id }
                val available = allFriends.filter { it.id !in participantIds }

                _uiState.update { currentState ->
                    currentState.copy(
                        availableFriends = available,
                        showBottomSheet = true
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "Erro ao carregar amigos disponíveis")
            }
        }
    }

    fun onConfirmParticipants(selectedIds: List<Int>) {
        viewModelScope.launch {
            addParticipantsUseCase(checkId, selectedIds)
            onDismissBottomSheet()
        }
    }

    fun onRemoveParticipant(friendId: Int) {
        viewModelScope.launch {
            removeParticipantUseCase(checkId, friendId)
        }
    }

    fun onNameChanged(newName: String) {
        val currentCheck = uiState.value.check ?: return
        viewModelScope.launch {
            when (val result = updateCheckUseCase(currentCheck, newName)) {
                is UpdateCheckResult.Success -> Timber.i("Nome atualizado")
                is UpdateCheckResult.Error -> _uiEvent.emit(CheckDetailUiEvent.ShowSnackbar(result.message))
            }
        }
    }
}