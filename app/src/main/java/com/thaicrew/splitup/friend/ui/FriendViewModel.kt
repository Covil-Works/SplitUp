package com.thaicrew.splitup.friend.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thaicrew.splitup.friend.domain.AddFriendUseCase
import com.thaicrew.splitup.friend.domain.Friend
import com.thaicrew.splitup.friend.domain.FriendAlreadyExistsException
import com.thaicrew.splitup.friend.domain.FriendAlreadyInactiveException
import com.thaicrew.splitup.friend.domain.FriendNotFoundException
import com.thaicrew.splitup.friend.domain.GetActiveFriendsUseCase
import com.thaicrew.splitup.friend.domain.InvalidFriendNameException
import com.thaicrew.splitup.friend.domain.SoftDeleteFriendUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

@HiltViewModel
class FriendViewModel @Inject constructor(
    // Todos os use cases deverão estar aqui
    private val getActiveFriendsUseCase: GetActiveFriendsUseCase,
    private val addFriendUseCase: AddFriendUseCase,
    private val softDeleteFriendUseCase: SoftDeleteFriendUseCase

) : ViewModel() {

    private val _uiState = MutableStateFlow(FriendScreenState())
    val uiState: StateFlow<FriendScreenState> = _uiState.asStateFlow()
    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    init {
        observeFriends()
    }

    private fun observeFriends() {
        getActiveFriendsUseCase()
            .onEach { friends ->
                _uiState.update { currentState ->
                    currentState.copy(friends = friends, isLoading = false)
                }
            }
            .launchIn(viewModelScope)
    }

    /**
     * Função chamada pela UI para tratar do evento de adicionar um novo amigo.
     */
    fun onAddFriend(name: String) {
        viewModelScope.launch {
            try {
                addFriendUseCase(name)
            } catch (e: InvalidFriendNameException) {
                _uiEvent.emit(UiEvent.ShowSnackbar(e.message ?: "Amigo não encontrado"))
            } catch (e: FriendAlreadyExistsException) {
                _uiEvent.emit(UiEvent.ShowSnackbar(e.message ?: "Amigo já existe"))
            }
        }
    }

    fun onSoftDeleteTriggered(friend: Friend) {
        _uiState.update { it.copy(dialogState = DialogState.ConfirmDeactivation(friend)) }
    }

    fun onDialogDismiss() {
        _uiState.update { it.copy(dialogState = DialogState.Hidden) }
    }

    fun onSoftDeleteConfirmed() {
        val friendToDelete = (uiState.value.dialogState as? DialogState.ConfirmDeactivation)?.friend

        if (friendToDelete != null) {
            viewModelScope.launch {
                try {
                    softDeleteFriendUseCase(friendToDelete.id)
                } catch (e: FriendNotFoundException) {
                    _uiEvent.emit(UiEvent.ShowSnackbar(e.message ?: "Amigo não encontrado"))
                } catch (e: FriendAlreadyInactiveException) {
                    _uiEvent.emit(UiEvent.ShowSnackbar(e.message ?: "Amigo já está inativo"))
                } finally {
                    onDialogDismiss()
                }
            }
        }

    }
}