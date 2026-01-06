package com.thaicrew.splitup.check.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thaicrew.splitup.check.domain.AddItemUseCase
import com.thaicrew.splitup.check.domain.AddParticipantsUseCase
import com.thaicrew.splitup.check.domain.GetCheckByIdFlowUseCase
import com.thaicrew.splitup.check.domain.GetItemsByCheckUseCase
import com.thaicrew.splitup.check.domain.UpdateCheckResult
import com.thaicrew.splitup.check.domain.UpdateNameCheckUseCase
import com.thaicrew.splitup.check.domain.GetParticipantsUseCase
import com.thaicrew.splitup.check.domain.RemoveParticipantUseCase
import com.thaicrew.splitup.check.domain.ToggleItemShareUseCase
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
    private val getActiveFriendsUseCase: GetActiveFriendsUseCase,
    private val addItemUseCase: AddItemUseCase,
    private val toggleItemShareUseCase: ToggleItemShareUseCase,
    private val getItemsByCheckUseCase: GetItemsByCheckUseCase

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
        observeItems()
    }

    private fun observeItems(){
        getItemsByCheckUseCase(checkId)
            .onEach { items -> _uiState.update {it.copy(items = items)}
            }
            .launchIn(viewModelScope)
    }

    fun onNewItemNameChanged(newName: String) {
        _uiState.update { it.copy(newItemName = newName) }
    }

    fun onNewItemQuantityChanged(delta: Int) {
        _uiState.update { state ->
            val newQty = state.newItemQuantity + delta
            if (newQty >= 1) state.copy(newItemQuantity = newQty) else state
        }
    }

    fun onNewItemValueChanged(newValue: String) {
        _uiState.update { it.copy(newItemValue = newValue) }
    }

    fun onToggleFriendSelection(friendId: Int) {
        _uiState.update { state ->
            val currentSelection = state.selectedFriendIdsForItem.toMutableSet()
            if (currentSelection.contains(friendId)) {
                currentSelection.remove(friendId)
            } else {
                currentSelection.add(friendId)
            }
            state.copy(selectedFriendIdsForItem = currentSelection)
        }
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

    fun onAddItemClicked() {
        val state = uiState.value

        // remove tudo que não for número para pegar os centavos
        val cleanString = state.newItemValue.replace(Regex("[^0-9]"), "")
        val valueInCents = cleanString.toLongOrNull() ?: 0L

        // Validações básicas antes de chamar o domínio
        if (state.newItemName.isBlank()) return
        if (valueInCents <= 0) {
            viewModelScope.launch { _uiEvent.emit(CheckDetailUiEvent.ShowSnackbar("O valor deve ser maior que zero.")) }
            return
        }
        if (state.selectedFriendIdsForItem.isEmpty()) {
            viewModelScope.launch { _uiEvent.emit(CheckDetailUiEvent.ShowSnackbar("Selecione quem divide este item.")) }
            return
        }

        viewModelScope.launch {
            try {
                // 1. Cria o Item
                val newItemId = addItemUseCase(
                    chekId = checkId,
                    itemName = state.newItemName,
                    itemQuantity = state.newItemQuantity,
                    itemValueInCents = valueInCents
                ).toInt()

                // 2. Cria os Vínculos (Quem paga)
                state.selectedFriendIdsForItem.forEach { friendId ->
                    toggleItemShareUseCase(
                        itemId = newItemId,
                        friendId = friendId,
                        checkId = checkId,
                        isShared = true
                    )
                }

                // 3. Limpa o Formulário
                _uiState.update { it.copy(
                    newItemName = "",
                    newItemQuantity = 1,
                    newItemValue = "",
                    selectedFriendIdsForItem = emptySet()
                )}

            } catch (e: Exception) {
                Timber.e(e, "Erro ao adicionar item")
                _uiEvent.emit(CheckDetailUiEvent.ShowSnackbar("Erro ao adicionar item."))
            }
        }
    }

}