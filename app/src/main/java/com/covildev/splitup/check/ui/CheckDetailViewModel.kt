package com.covildev.splitup.check.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.covildev.splitup.check.domain.AddItemUseCase
import com.covildev.splitup.check.domain.AddParticipantsUseCase
import com.covildev.splitup.check.domain.CalculateCheckTotalUseCase
import com.covildev.splitup.check.domain.CloseCheckUseCase
import com.covildev.splitup.check.domain.DeleteItemUseCase
import com.covildev.splitup.check.domain.GetCheckByIdFlowUseCase
import com.covildev.splitup.check.domain.GetCheckItemsWithSharersUseCase
import com.covildev.splitup.check.domain.GetItemsByCheckUseCase
import com.covildev.splitup.check.domain.UpdateCheckResult
import com.covildev.splitup.check.domain.UpdateNameCheckUseCase
import com.covildev.splitup.check.domain.GetParticipantsUseCase
import com.covildev.splitup.check.domain.ItemWithSharers
import com.covildev.splitup.check.domain.RemoveParticipantUseCase
import com.covildev.splitup.check.domain.ToggleItemShareUseCase
import com.covildev.splitup.check.domain.UpdateItemUseCase
import com.covildev.splitup.common.utils.CurrencyUtils
import com.covildev.splitup.friend.domain.GetActiveFriendsUseCase
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
    private val deleteItemUseCase: DeleteItemUseCase,
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

                // 1. Calcula os totais por amigo em memÃ³ria
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
                // Retorno automÃ¡tico ao estado TODOS
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
                Timber.e(e, "Erro ao carregar amigos disponÃ­veis")
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

        // 1. Verifica se o amigo estÃ¡ na lista de pagantes de ALGUM item da comanda
        val isSharingAnyItem = state.itemsWithSharers.any { it.sharersIds.contains(friendId) }

        if (isSharingAnyItem) {
            // 2. Pega o nome para exibir na mensagem (UX melhor)
            val friendName = state.participants.find { it.id == friendId }?.name ?: "esse participante"

            // 3. Aborta a remoÃ§Ã£o e mostra dialog informativo
            _uiState.update {
                it.copy(infoDialogMessage = "NÃ£o Ã© possÃ­vel remover o amigo $friendName porque ele jÃ¡ divide um item nesta comanda.")
            }
            return
        }

        // 4. Se nÃ£o estiver dividindo nada, prossegue com a remoÃ§Ã£o
        viewModelScope.launch {
            removeParticipantUseCase(checkId, friendId)
        }
    }

    fun onDismissInfoDialog() {
        _uiState.update { it.copy(infoDialogMessage = null) }
    }

    fun onShowEditNameDialog() {
        _uiState.update { it.copy(showEditNameDialog = true) }
    }

    fun onDismissEditNameDialog() {
        _uiState.update { it.copy(showEditNameDialog = false) }
    }

    fun onNameChanged(newName: String) {
        val currentCheck = uiState.value.check ?: return
        viewModelScope.launch {
            when (val result = updateCheckUseCase(currentCheck, newName)) {
                is UpdateCheckResult.Success -> Timber.i("Nome atualizado")
                is UpdateCheckResult.Error -> Timber.e("Erro ao atualizar nome: ${result.message}")
            }
        }
    }

    fun onAddItemClicked() {
        val state = uiState.value
        val valueInCents = CurrencyUtils.parseToCents(state.newItemValue)

        // Determina quem vai pagar
        val targetFriendIds = if (state.isAllSelected) {
            state.participants.map { it.id }
        } else {
            state.selectedFriendIdsForItem.toList()
        }

        // ValidaÃ§Ãµes
        if (state.newItemName.isBlank()) return
        if (valueInCents <= 0) {
            return
        }
        if (targetFriendIds.isEmpty()) {
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
                    isAllSelected = true // Volta para o padrÃ£o
                )}

            } catch (e: Exception) {
                Timber.e(e, "Erro ao adicionar item")
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

    fun onExpandItem(itemId: Int) {
        // Se clicar no mesmo que jÃ¡ tÃ¡ aberto, fecha. Se nÃ£o, abre o novo.
        _uiState.update { state ->
            if (state.expandedItemId == itemId) {
                state.copy(expandedItemId = null, isEditing = false)
            } else {
                state.copy(expandedItemId = itemId, isEditing = false)
            }
        }
    }

    fun onStartEditItem(itemWithSharers: ItemWithSharers) {
        val item = itemWithSharers.item
        // editingValue Ã© em REAIS (string), para ser compatÃ­vel com parseToCents()
        val valueString = CurrencyUtils.formatFromCents(item.valueInCents)
        val sharersSnapshot = itemWithSharers.sharersIds.toSet()

        _uiState.update {
            it.copy(
                isEditing = true,
                // Carrega explicitamente os dados atuais do item para o formulÃ¡rio
                editingName = item.name,
                editingQuantity = item.quantity,
                editingValue = valueString,
                // IMPORTANTe: mantÃ©m os pagantes preenchidos para o save
                editingSharers = sharersSnapshot,
                // Snapshot para comparaÃ§Ã£o/fallback
                originalEditingSharers = sharersSnapshot
            )
        }
    }

    fun onCancelEdit() {
        _uiState.update { it.copy(isEditing = false, originalEditingSharers = emptySet()) }
    }

    fun onCollapseItem() {
        _uiState.update {
            it.copy(
                expandedItemId = null,
                isEditing = false,
                originalEditingSharers = emptySet()
            )
        }
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
        val itemId = state.expandedItemId ?: return

        // Encontra o item original na lista para podermos comparar os pagantes
        val originalEntry = state.itemsWithSharers.find { it.item.id == itemId } ?: return

        // Tratamento do valor (String -> Long)
        val valueInCents = CurrencyUtils.parseToCents(state.editingValue)

        // ValidaÃ§Ã£o bÃ¡sica
        if (state.editingName.isBlank()) return
        if (valueInCents <= 0) {
            return
        }

        // Fallback robusto: se por qualquer motivo o set vier vazio, nÃ£o zera pagantes
        val effectiveSharers = when {
            state.editingSharers.isNotEmpty() -> state.editingSharers
            state.originalEditingSharers.isNotEmpty() -> state.originalEditingSharers
            else -> originalEntry.sharersIds.toSet()
        }

        if (effectiveSharers.isEmpty()) {
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
                val oldSharers = originalEntry.sharersIds.toSet()
                val newSharers = effectiveSharers

                // Quem entrou na divisÃ£o?
                val toAdd = newSharers - oldSharers
                toAdd.forEach { friendId ->
                    toggleItemShareUseCase(itemId, friendId, checkId, isShared = true)
                }

                // Quem saiu da divisÃ£o?
                val toRemove = oldSharers - newSharers
                toRemove.forEach { friendId ->
                    toggleItemShareUseCase(itemId, friendId, checkId, isShared = false)
                }

                // 3. Fecha o modo de ediÃ§Ã£o
                onCollapseItem()

            } catch (e: Exception) {
                Timber.e(e, "Erro ao atualizar item")
            }
        }
    }

    fun onDeleteEditClicked() {
        val state = uiState.value
        val itemId = state.expandedItemId ?: return
        val originalEntry = state.itemsWithSharers.find { it.item.id == itemId } ?: return

        viewModelScope.launch {
            try {
                // Ao deletar o item, o Room (Cascade) jÃ¡ remove as relaÃ§Ãµes de share automaticamente.
                deleteItemUseCase(originalEntry.item)

                // Fecha o modo de ediÃ§Ã£o
                onCollapseItem()

            } catch (e: Exception) {
                Timber.e(e, "Erro ao excluir item")
            }
        }
    }

    fun onCloseCheckClicked() {
        if (uiState.value.items.isEmpty()) {
            return
        }
        // Se tiver itens, prossegue com o fluxo normal (abrir diÃ¡logo)
        _uiState.update { it.copy(showCloseCheckDialog = true) }
    }

    fun onDismissCloseCheckDialog() {
        _uiState.update { it.copy(showCloseCheckDialog = false) }
    }

    fun onCloseCheckConfirmed() {
        val currentCheck = uiState.value.check ?: return

        // Fecha o diÃ¡logo imediatamente
        onDismissCloseCheckDialog()

        viewModelScope.launch {
            try {
                // Chama o UseCase criado no Passo 1
                closeCheckUseCase(currentCheck)

                // Sucesso: Notifica o usuÃ¡rio e volta para a tela anterior
                _uiEvent.emit(CheckDetailUiEvent.NavigateBack)
            } catch (e: Exception) {
                Timber.e(e, "Erro ao fechar comanda")
            }
        }
    }

    fun onFriendTotalClicked(friendId: Int) {
        _uiState.update { state ->
            // Se clicar no mesmo amigo expandido, recolhe.
            if (state.expandedFriendId == friendId) {
                state.copy(
                    expandedFriendId = null,
                    expandedFriendOwedItems = emptyList()
                )
            } else {
                val owedItems = calculateOwedItemsForFriend(friendId, state.itemsWithSharers)
                state.copy(
                    expandedFriendId = friendId,
                    expandedFriendOwedItems = owedItems
                )
            }
        }
    }

    private fun calculateOwedItemsForFriend(
        friendId: Int,
        items: List<ItemWithSharers>
    ): List<FriendOwedItem> {
        // Regra: cada item Ã© dividido igualmente entre os sharers.
        // O valor do amigo = (valor unitÃ¡rio * quantidade) / N
        // Considera a multiplicaÃ§Ã£o (quantidade) no total.
        return items
            .asSequence()
            .filter { it.sharersIds.contains(friendId) }
            .mapNotNull { entry ->
                val sharersCount = entry.sharersIds.size
                if (sharersCount <= 0) return@mapNotNull null

                val totalItemValue = entry.item.valueInCents * entry.item.quantity
                val sharePerPerson = totalItemValue / sharersCount

                FriendOwedItem(
                    itemId = entry.item.id,
                    itemName = entry.item.name,
                    quantity = entry.item.quantity,
                    unitValueInCents = entry.item.valueInCents,
                    amountInCents = sharePerPerson
                )
            }
            .sortedByDescending { it.amountInCents }
            .toList()
    }
}


