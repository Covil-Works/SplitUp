package com.thaicrew.splitup.check.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thaicrew.splitup.check.domain.AddItemUseCase
import com.thaicrew.splitup.check.domain.AddParticipantsUseCase
import com.thaicrew.splitup.check.domain.CalculateCheckTotalUseCase
import com.thaicrew.splitup.check.domain.CloseCheckUseCase
import com.thaicrew.splitup.check.domain.GetCheckByIdFlowUseCase
import com.thaicrew.splitup.check.domain.GetCheckItemsWithSharersUseCase
import com.thaicrew.splitup.check.domain.GetItemsByCheckUseCase
import com.thaicrew.splitup.check.domain.UpdateCheckResult
import com.thaicrew.splitup.check.domain.UpdateNameCheckUseCase
import com.thaicrew.splitup.check.domain.GetParticipantsUseCase
import com.thaicrew.splitup.check.domain.ItemWithSharers
import com.thaicrew.splitup.check.domain.RemoveParticipantUseCase
import com.thaicrew.splitup.check.domain.ToggleItemShareUseCase
import com.thaicrew.splitup.check.domain.UpdateItemUseCase
import com.thaicrew.splitup.common.utils.CurrencyUtils
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
    private val getItemsByCheckUseCase: GetItemsByCheckUseCase,
    private val getCheckItemsWithSharersUseCase: GetCheckItemsWithSharersUseCase,
    private val updateItemUseCase: UpdateItemUseCase,
    private val deleteItemUseCase: UpdateItemUseCase,
    private val closeCheckUseCase: CloseCheckUseCase,
    private val calculateCheckTotalUseCase: CalculateCheckTotalUseCase

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

    private fun observeItems() {
        // Observa o novo fluxo "rico" (Item + Sharers)
        getCheckItemsWithSharersUseCase(checkId)
            .onEach { itemsWithSharers ->

                // 1. Calcula os totais por amigo em memória
                val totalsMap = calculateFriendTotals(itemsWithSharers)
                val totalCheckValue = calculateCheckTotalUseCase(itemsWithSharers.map { it.item })

                // 2. Atualiza o estado com a lista rica e os totais calculados
                _uiState.update {
                    it.copy(
                        itemsWithSharers = itemsWithSharers,
                        friendTotals = totalsMap,
                        items = itemsWithSharers.map { it.item },
                        checkTotal = totalCheckValue
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun onChangeViewMode(mode: CheckViewMode) {
        _uiState.update { it.copy(viewMode = mode) }
    }

    private fun calculateFriendTotals(items: List<ItemWithSharers>): Map<Int, Long> {
        val totals = mutableMapOf<Int, Long>()

        items.forEach { entry ->
            val totalItemValue = entry.item.valueInCents * entry.item.quantity
            val sharersCount = entry.sharersIds.size

            if (sharersCount > 0) {
                val sharePerPerson = totalItemValue / sharersCount

                entry.sharersIds.forEach { friendId ->
                    val currentTotal = totals.getOrDefault(friendId, 0L)
                    totals[friendId] = currentTotal + sharePerPerson
                }
            }
        }
        return totals
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

            // Verifica se selecionou todos manualmente
            val allParticipantsIds = state.participants.map { it.id }.toSet()
            val isFullSelection = currentSelection.containsAll(allParticipantsIds) &&
                    currentSelection.size == allParticipantsIds.size

            if (isFullSelection) {
                // Retorno automático ao estado TODOS
                state.copy(
                    isAllSelected = true,
                    selectedFriendIdsForItem = emptySet() // Limpa o set pois a flag manda
                )
            } else {
                state.copy(
                    isAllSelected = false,
                    selectedFriendIdsForItem = currentSelection
                )
            }
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
        val state = uiState.value

        // 1. Verifica se o amigo está na lista de pagantes de ALGUM item da comanda
        val isSharingAnyItem = state.itemsWithSharers.any { it.sharersIds.contains(friendId) }

        if (isSharingAnyItem) {
            // 2. Pega o nome para exibir na mensagem (UX melhor)
            val friendName = state.participants.find { it.id == friendId }?.name ?: "esse participante"

            // 3. Emite o erro e aborta a remoção
            viewModelScope.launch {
                _uiEvent.emit(CheckDetailUiEvent.ShowSnackbar("Não é possível remover $friendName, ele está dividindo um item."))
            }
            return
        }

        // 4. Se não estiver dividindo nada, prossegue com a remoção
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
        val cleanString = state.newItemValue.replace(Regex("[^0-9]"), "")
        val valueInCents = CurrencyUtils.parseToCents(state.newItemValue)

        // Determina quem vai pagar
        val targetFriendIds = if (state.isAllSelected) {
            state.participants.map { it.id }
        } else {
            state.selectedFriendIdsForItem.toList()
        }

        // Validações
        if (state.newItemName.isBlank()) return
        if (valueInCents <= 0) {
            viewModelScope.launch { _uiEvent.emit(CheckDetailUiEvent.ShowSnackbar("O valor deve ser maior que zero.")) }
            return
        }
        if (targetFriendIds.isEmpty()) {
            viewModelScope.launch { _uiEvent.emit(CheckDetailUiEvent.ShowSnackbar("Selecione quem divide este item.")) }
            return
        }

        viewModelScope.launch {
            try {
                val newItemId = addItemUseCase(
                    chekId = checkId,
                    itemName = state.newItemName,
                    itemQuantity = state.newItemQuantity,
                    itemValueInCents = valueInCents
                ).toInt()

                // Usa a lista calculada
                targetFriendIds.forEach { friendId ->
                    toggleItemShareUseCase(
                        itemId = newItemId,
                        friendId = friendId,
                        checkId = checkId,
                        isShared = true
                    )
                }

                // Reseta para o TODOS
                _uiState.update { it.copy(
                    newItemName = "",
                    newItemQuantity = 1,
                    newItemValue = "",
                    selectedFriendIdsForItem = emptySet(),
                    isAllSelected = true // Volta para o padrão
                )}

            } catch (e: Exception) {
                Timber.e(e, "Erro ao adicionar item")
                _uiEvent.emit(CheckDetailUiEvent.ShowSnackbar("Erro ao adicionar item."))
            }
        }
    }

    fun onRemoveAllSelection() {
        _uiState.update {
            it.copy(
                isAllSelected = false,
                selectedFriendIdsForItem = emptySet()
            )
        }
    }

    fun onExpandItem(itemWithSharers: com.thaicrew.splitup.check.domain.ItemWithSharers) {
        val item = itemWithSharers.item
        // Converte centavos para String (ex: 1050 -> "1050" ou "10.50")
        // Para manter simples e compatível com a lógica de filtro de digitos, usaremos apenas números
        val valueString = CurrencyUtils.formatFromCents(item.valueInCents)

        _uiState.update { it.copy(
            editingItemId = item.id,
            editingName = item.name,
            editingQuantity = item.quantity,
            editingValue = valueString,
            editingSharers = itemWithSharers.sharersIds.toSet()
        )}
    }

    fun onCollapseItem() {
        _uiState.update { it.copy(editingItemId = null) }
    }

    fun onEditNameChange(newName: String) {
        _uiState.update { it.copy(editingName = newName) }
    }

    fun onEditQuantityChange(delta: Int) {
        _uiState.update { state ->
            val newQty = state.editingQuantity + delta
            if (newQty >= 1) state.copy(editingQuantity = newQty) else state
        }
    }

    fun onEditValueChange(newValue: String) {
        _uiState.update { it.copy(editingValue = newValue) }
    }

    fun onEditToggleFriend(friendId: Int) {
        _uiState.update { state ->
            val current = state.editingSharers.toMutableSet()
            if (current.contains(friendId)) current.remove(friendId) else current.add(friendId)
            state.copy(editingSharers = current)
        }
    }

    fun onSaveEditClicked() {
        val state = uiState.value
        val itemId = state.editingItemId ?: return

        // Encontra o item original na lista para podermos comparar os pagantes
        val originalEntry = state.itemsWithSharers.find { it.item.id == itemId } ?: return

        // Tratamento do valor (String -> Long)
        val cleanString = state.editingValue.replace(Regex("[^0-9]"), "")
        val valueInCents = CurrencyUtils.parseToCents(state.editingValue)

        // Validação básica
        if (state.editingName.isBlank()) return
        if (valueInCents <= 0) {
            viewModelScope.launch { _uiEvent.emit(CheckDetailUiEvent.ShowSnackbar("O valor deve ser maior que zero.")) }
            return
        }
        if (state.editingSharers.isEmpty()) {
            viewModelScope.launch { _uiEvent.emit(CheckDetailUiEvent.ShowSnackbar("O item precisa ter pelo menos um pagante.")) }
            return
        }

        viewModelScope.launch {
            try {
                // 1. Atualiza os dados do Item
                val updatedItem = originalEntry.item.copy(
                    name = state.editingName,
                    quantity = state.editingQuantity,
                    valueInCents = valueInCents
                )
                updateItemUseCase(updatedItem)

                // 2. Sincroniza os Pagantes (Quem divide)
                // Compara a lista antiga (originalEntry.sharersIds) com a nova (state.editingSharers)
                val oldSharers = originalEntry.sharersIds.toSet()
                val newSharers = state.editingSharers

                // Quem entrou na divisão?
                val toAdd = newSharers - oldSharers
                toAdd.forEach { friendId ->
                    toggleItemShareUseCase(itemId, friendId, checkId, isShared = true)
                }

                // Quem saiu da divisão?
                val toRemove = oldSharers - newSharers
                toRemove.forEach { friendId ->
                    toggleItemShareUseCase(itemId, friendId, checkId, isShared = false)
                }

                // 3. Fecha o modo de edição
                onCollapseItem()

            } catch (e: Exception) {
                Timber.e(e, "Erro ao atualizar item")
                _uiEvent.emit(CheckDetailUiEvent.ShowSnackbar("Erro ao salvar alterações."))
            }
        }
    }

    fun onDeleteEditClicked() {
        val state = uiState.value
        val itemId = state.editingItemId ?: return
        val originalEntry = state.itemsWithSharers.find { it.item.id == itemId } ?: return

        viewModelScope.launch {
            try {
                // Ao deletar o item, o Room (Cascade) já remove as relações de share automaticamente.
                deleteItemUseCase(originalEntry.item)

                // Fecha o modo de edição
                onCollapseItem()

            } catch (e: Exception) {
                Timber.e(e, "Erro ao excluir item")
                _uiEvent.emit(CheckDetailUiEvent.ShowSnackbar("Erro ao excluir item."))
            }
        }
    }

    fun onCloseCheckClicked() {
        if (uiState.value.items.isEmpty()) {
            viewModelScope.launch {
                _uiEvent.emit(CheckDetailUiEvent.ShowSnackbar("Não é possível fechar uma comanda sem itens."))
            }
            return
        }
        // Se tiver itens, prossegue com o fluxo normal (abrir diálogo)
        _uiState.update { it.copy(showCloseCheckDialog = true) }
    }

    fun onDismissCloseCheckDialog() {
        _uiState.update { it.copy(showCloseCheckDialog = false) }
    }

    fun onCloseCheckConfirmed() {
        val currentCheck = uiState.value.check ?: return

        // Fecha o diálogo imediatamente
        onDismissCloseCheckDialog()

        viewModelScope.launch {
            try {
                // Chama o UseCase criado no Passo 1
                closeCheckUseCase(currentCheck)

                // Sucesso: Notifica o usuário e volta para a tela anterior
                _uiEvent.emit(CheckDetailUiEvent.NavigateBack)
            } catch (e: Exception) {
                Timber.e(e, "Erro ao fechar comanda")
                _uiEvent.emit(CheckDetailUiEvent.ShowSnackbar("Erro ao fechar comanda."))
            }
        }
    }
}