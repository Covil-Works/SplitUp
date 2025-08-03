package com.thaicrew.splitup.friend.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thaicrew.splitup.friend.domain.AddFriendResult
import com.thaicrew.splitup.friend.domain.AddFriendUseCase
import com.thaicrew.splitup.friend.domain.Friend
import com.thaicrew.splitup.friend.domain.UpdateFriendResult
import com.thaicrew.splitup.friend.domain.UpdateFriendUseCase
import com.thaicrew.splitup.friend.domain.GetActiveFriendsUseCase
import com.thaicrew.splitup.friend.domain.ReactivateAddFriendUseCase
import com.thaicrew.splitup.friend.domain.SoftDeleteFriendUseCase
import com.thaicrew.splitup.friend.domain.SoftDeleteFriendUseCaseResult
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class FriendViewModel @Inject constructor(
    private val getActiveFriendsUseCase: GetActiveFriendsUseCase,
    private val addFriendUseCase: AddFriendUseCase,
    private val softDeleteFriendUseCase: SoftDeleteFriendUseCase,
    private val reactivateFriendUseCase: ReactivateAddFriendUseCase,
    private val updateFriendUseCase: UpdateFriendUseCase
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
     * Agora usa a lógica de resultado explícito com a sealed class.
     */
    fun onAddFriend(name: String) {
        viewModelScope.launch {
            when (val result = addFriendUseCase(name)) {
                is AddFriendResult.Success -> {
                    _uiEvent.emit(UiEvent.ShowSnackbar("Amigo adicionado!"))
                }

                is AddFriendResult.AlreadyExistsActive -> {
                    _uiEvent.emit(UiEvent.ShowSnackbar("O amigo '${result.friend.name}' já está na lista."))
                }

                is AddFriendResult.NeedsReactivation -> {
                    _uiState.update { it.copy(dialogState = DialogState.ConfirmReactivation(result.friend)) }
                }

                is AddFriendResult.Error -> {
                    _uiEvent.emit(
                        UiEvent.ShowSnackbar(
                            result.exception.message ?: "Ocorreu um erro."
                        )
                    )
                }
            }
        }
    }

    fun onSoftDeleteTriggered(friend: Friend) {
        _uiState.update { it.copy(dialogState = DialogState.ConfirmDeactivation(friend)) }
    }

    fun onDialogDismiss() {
        _uiState.update { it.copy(dialogState = DialogState.Hidden) }
    }

    /**
     * Chamado quando o usuário confirma a reativação no diálogo.
     */
    fun onReactivationConfirmed() {
        val friendToReactivate =
            (uiState.value.dialogState as? DialogState.ConfirmReactivation)?.friend
        onDialogDismiss()

        if (friendToReactivate != null) {
            viewModelScope.launch {
                reactivateFriendUseCase(friendToReactivate)
                _uiEvent.emit(UiEvent.ShowSnackbar("O amigo '${friendToReactivate.name}' foi reativado!"))
            }
        }
    }

    fun onSoftDeleteConfirmed() {
        val friendToDelete = (uiState.value.dialogState as? DialogState.ConfirmDeactivation)?.friend
        onDialogDismiss()

        if (friendToDelete != null) {
            viewModelScope.launch {
                // O bloco try-catch foi substituído por um 'when' mais limpo e seguro
                when (softDeleteFriendUseCase(friendToDelete.id)) {
                    is SoftDeleteFriendUseCaseResult.Success -> {
                        _uiEvent.emit(UiEvent.ShowSnackbar("Amigo desativado com sucesso."))
                    }

                    is SoftDeleteFriendUseCaseResult.FriendNotFound -> {
                        _uiEvent.emit(UiEvent.ShowSnackbar("Erro: Amigo não encontrado."))
                    }

                    is SoftDeleteFriendUseCaseResult.AlreadyInactive -> {
                        _uiEvent.emit(UiEvent.ShowSnackbar("Este amigo já estava desativado."))
                    }
                }
            }
        }
    }

    /**
     * Chamado pela UI quando o botão de editar é clicado.
     * Atualiza o estado para mostrar o diálogo de edição.
     */
    fun onEditTriggered(friend: Friend) {
        _uiState.update { it.copy(dialogState = DialogState.ShowEdit(friend)) }
    }

    /**
     * Chamado quando o usuário confirma a edição no diálogo.
     * Invoca o caso de uso e trata o resultado.
     */
    fun onEditConfirmed(friend: Friend, newName: String) {
        viewModelScope.launch {
            when (val result = updateFriendUseCase(friend, newName)) {
                is UpdateFriendResult.Success -> {
                    _uiEvent.emit(UiEvent.ShowSnackbar("Amigo atualizado com sucesso!"))
                    onDialogDismiss() // Fecha o diálogo em caso de sucesso
                }
                is UpdateFriendResult.Error -> {
                    _uiEvent.emit(
                        UiEvent.ShowSnackbar(
                            result.exception.message ?: "Ocorreu um erro desconhecido."
                        )
                    )
                    // Opcional: manter o diálogo aberto em caso de erro para o usuário corrigir.
                }
            }
        }
    }
}
