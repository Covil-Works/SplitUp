package com.thaicrew.splitup.friend.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thaicrew.splitup.friend.domain.AddFriendResult
import com.thaicrew.splitup.friend.domain.AddFriendUseCase
import com.thaicrew.splitup.friend.domain.Friend
import com.thaicrew.splitup.friend.domain.GetActiveFriendsUseCase
import com.thaicrew.splitup.friend.domain.ReactivateAddFriendUseCase
import com.thaicrew.splitup.friend.domain.SoftDeleteFriendUseCase
import com.thaicrew.splitup.friend.domain.SoftDeleteFriendUseCaseResult
import com.thaicrew.splitup.friend.domain.UpdateFriendResult
import com.thaicrew.splitup.friend.domain.UpdateFriendUseCase
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
import timber.log.Timber

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
        Timber.i("ViewModel inicializada.")
        observeFriends()
    }

    private fun observeFriends() {
        Timber.d("Iniciando observação do fluxo de amigos ativos.")
        getActiveFriendsUseCase()
            .onEach { friends ->
                Timber.d("Fluxo de amigos emitiu uma nova lista com ${friends.size} amigos.")
                _uiState.update { currentState ->
                    currentState.copy(friends = friends, isLoading = false)
                }
            }
            .launchIn(viewModelScope)
    }

    fun onAddFriend(name: String) {
        Timber.i("Evento 'onAddFriend' recebido com o nome: '$name'")
        viewModelScope.launch {
            when (val result = addFriendUseCase(name)) {
                is AddFriendResult.Success -> {
                    Timber.d("Caso de uso 'addFriend' retornou Success.")
                    _uiEvent.emit(UiEvent.ShowSnackbar("Amigo adicionado!"))
                }

                is AddFriendResult.AlreadyExistsActive -> {
                    Timber.d("Caso de uso 'addFriend' retornou AlreadyExistsActive para o amigo: '${result.friend.name}'.")
                    _uiEvent.emit(UiEvent.ShowSnackbar("O amigo '${result.friend.name}' já está na lista."))
                }

                is AddFriendResult.NeedsReactivation -> {
                    Timber.d("Caso de uso 'addFriend' retornou NeedsReactivation. Atualizando estado para mostrar diálogo.")
                    _uiState.update { it.copy(dialogState = DialogState.ConfirmReactivation(result.friend)) }
                }

                is AddFriendResult.Error -> {
                    Timber.e(result.exception, "Caso de uso 'addFriend' retornou um erro.")
                    _uiEvent.emit(
                        UiEvent.ShowSnackbar(result.exception.message ?: "Ocorreu um erro.")
                    )
                }
            }
        }
    }

    fun onSoftDeleteTriggered(friend: Friend) {
        Timber.i("Evento 'onSoftDeleteTriggered' recebido para o amigo '${friend.name}' (ID: ${friend.id}).")
        _uiState.update { it.copy(dialogState = DialogState.ConfirmDeactivation(friend)) }
    }

    fun onDialogDismiss() {
        Timber.d("Evento 'onDialogDismiss' recebido. Ocultando diálogo.")
        _uiState.update { it.copy(dialogState = DialogState.Hidden) }
    }

    fun onReactivationConfirmed() {
        val friendToReactivate = (uiState.value.dialogState as? DialogState.ConfirmReactivation)?.friend
        Timber.i("Evento 'onReactivationConfirmed' recebido para o amigo: ${friendToReactivate?.name}")
        onDialogDismiss()

        if (friendToReactivate != null) {
            viewModelScope.launch {
                reactivateFriendUseCase(friendToReactivate)
                _uiEvent.emit(UiEvent.ShowSnackbar("O amigo '${friendToReactivate.name}' foi reativado!"))
            }
        } else {
            Timber.w("onReactivationConfirmed foi chamado, mas o amigo a ser reativado era nulo no estado.")
        }
    }

    fun onSoftDeleteConfirmed() {
        val friendToDelete = (uiState.value.dialogState as? DialogState.ConfirmDeactivation)?.friend
        Timber.i("Evento 'onSoftDeleteConfirmed' recebido para o amigo: ${friendToDelete?.name}")
        onDialogDismiss()

        if (friendToDelete != null) {
            viewModelScope.launch {
                when (softDeleteFriendUseCase(friendToDelete.id)) {
                    is SoftDeleteFriendUseCaseResult.Success -> {
                        Timber.d("Soft delete do amigo ID ${friendToDelete.id} bem-sucedido.")
                        _uiEvent.emit(UiEvent.ShowSnackbar("Amigo desativado com sucesso."))
                    }
                    is SoftDeleteFriendUseCaseResult.FriendNotFound -> {
                        Timber.w("Soft delete falhou: amigo ID ${friendToDelete.id} não encontrado.")
                        _uiEvent.emit(UiEvent.ShowSnackbar("Erro: Amigo não encontrado."))
                    }
                    is SoftDeleteFriendUseCaseResult.AlreadyInactive -> {
                        Timber.d("Soft delete para o amigo ID ${friendToDelete.id} não foi necessário, já estava inativo.")
                        _uiEvent.emit(UiEvent.ShowSnackbar("Este amigo já estava desativado."))
                    }
                }
            }
        } else {
            Timber.w("onSoftDeleteConfirmed foi chamado, mas o amigo a ser desativado era nulo no estado.")
        }
    }

    fun onEditTriggered(friend: Friend) {
        Timber.i("Evento 'onEditTriggered' recebido para o amigo '${friend.name}' (ID: ${friend.id}).")
        _uiState.update { it.copy(dialogState = DialogState.ShowEdit(friend)) }
    }

    fun onEditConfirmed(friend: Friend, newName: String) {
        Timber.i("Evento 'onEditConfirmed' recebido para o amigo ID ${friend.id} com o novo nome: '$newName'")
        viewModelScope.launch {
            when (val result = updateFriendUseCase(friend, newName)) {
                is UpdateFriendResult.Success -> {
                    Timber.d("Atualização do amigo ID ${friend.id} bem-sucedida.")
                    _uiEvent.emit(UiEvent.ShowSnackbar("Amigo atualizado com sucesso!"))
                    onDialogDismiss()
                }
                is UpdateFriendResult.Error -> {
                    Timber.e(result.exception, "Falha ao atualizar o amigo ID ${friend.id}.")
                    _uiEvent.emit(
                        UiEvent.ShowSnackbar(result.exception.message ?: "Ocorreu um erro desconhecido.")
                    )
                }
            }
        }
    }
}