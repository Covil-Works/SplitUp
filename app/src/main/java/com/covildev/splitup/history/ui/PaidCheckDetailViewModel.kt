package com.covildev.splitup.history.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.covildev.splitup.check.domain.*
import com.covildev.splitup.check.ui.CheckViewMode
import com.covildev.splitup.check.ui.FriendOwedItem
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import com.covildev.splitup.export.domain.PrepareCheckExportDataUseCase
import com.covildev.splitup.export.data.PdfExportManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import timber.log.Timber

// Eventos de Navegação
sealed interface PaidCheckUiEvent {
    object NavigateBack : PaidCheckUiEvent
    data class ShareFile(val file: File) : PaidCheckUiEvent
}

@HiltViewModel
class PaidCheckDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getCheckByIdFlowUseCase: GetCheckByIdFlowUseCase,
    private val getCheckItemsWithSharersUseCase: GetCheckItemsWithSharersUseCase,
    private val getParticipantsUseCase: GetParticipantsUseCase,
    private val calculateCheckTotalUseCase: CalculateCheckTotalUseCase,
    private val deleteCheckUseCase: DeleteCheckUseCase,
    private val reopenCheckUseCase: ReopenCheckUseCase,
    private val prepareCheckExportDataUseCase: PrepareCheckExportDataUseCase,
    private val pdfExportManager: PdfExportManager
) : ViewModel() {

    private val checkId: Int = checkNotNull(savedStateHandle["checkId"])

    private val _uiState = MutableStateFlow(PaidCheckDetailState())
    val uiState: StateFlow<PaidCheckDetailState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<PaidCheckUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    init {
        observeData()
    }

    private fun observeData() {
        // Combina os fluxos para atualizar a UI sempre que algo mudar no DB
        combine(
            getCheckByIdFlowUseCase(checkId),
            getCheckItemsWithSharersUseCase(checkId),
            getParticipantsUseCase(checkId)
        ) { check, items, participants ->
            Triple(check, items, participants)
        }.onEach { (check, items, participants) ->
            if (check != null) {
                val totalValue = calculateCheckTotalUseCase(items.map { it.item })
                val totalsMap = calculateFriendTotals(items)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        check = check,
                        itemsWithSharers = items,
                        participants = participants,
                        checkTotal = totalValue,
                        friendTotals = totalsMap
                    )
                }
            }
        }.launchIn(viewModelScope)
    }

    // --- Lógica de Visualização ---
    fun onChangeViewMode(mode: CheckViewMode) {
        _uiState.update { it.copy(viewMode = mode) }
    }

    fun onExpandItem(itemId: Int) {
        _uiState.update { state ->
            if (state.expandedItemId == itemId) state.copy(expandedItemId = null)
            else state.copy(expandedItemId = itemId)
        }
    }

    fun onFriendTotalClicked(friendId: Int) {
        _uiState.update { state ->
            if (state.expandedFriendId == friendId) {
                state.copy(expandedFriendId = null, expandedFriendOwedItems = emptyList())
            } else {
                val owedItems = calculateOwedItemsForFriend(friendId, state.itemsWithSharers)
                state.copy(expandedFriendId = friendId, expandedFriendOwedItems = owedItems)
            }
        }
    }

    // --- Ações de Menu (Reabrir / Apagar) ---
    fun onDeleteClicked() { _uiState.update { it.copy(showDeleteDialog = true) } }
    fun onReopenClicked() { _uiState.update { it.copy(showReopenDialog = true) } }
    fun onDismissDialogs() { _uiState.update { it.copy(showDeleteDialog = false, showReopenDialog = false) } }

    fun onDeleteConfirmed() {
        val check = uiState.value.check ?: return
        onDismissDialogs()
        viewModelScope.launch {
            deleteCheckUseCase(check)
            _uiEvent.emit(PaidCheckUiEvent.NavigateBack)
        }
    }

    fun onReopenConfirmed() {
        val check = uiState.value.check ?: return
        onDismissDialogs()
        viewModelScope.launch {
            reopenCheckUseCase(check)
            _uiEvent.emit(PaidCheckUiEvent.NavigateBack) // Volta para lista, pois ela saiu do histórico
        }
    }

    // --- Helpers de Cálculo (Podem ser movidos para UseCases se preferir, mantive aqui para agilizar) ---
    private fun calculateFriendTotals(items: List<ItemWithSharers>): Map<Int, Long> {
        val totals = mutableMapOf<Int, Long>()
        items.forEach { entry ->
            val totalItemValue = entry.item.valueInCents * entry.item.quantity
            val sharersCount = entry.sharersIds.size
            if (sharersCount > 0) {
                val sharePerPerson = totalItemValue / sharersCount
                entry.sharersIds.forEach { friendId ->
                    totals[friendId] = totals.getOrDefault(friendId, 0L) + sharePerPerson
                }
            }
        }
        return totals
    }

    private fun calculateOwedItemsForFriend(friendId: Int, items: List<ItemWithSharers>): List<FriendOwedItem> {
        return items.asSequence()
            .filter { it.sharersIds.contains(friendId) }
            .mapNotNull { entry ->
                val sharersCount = entry.sharersIds.size
                if (sharersCount <= 0) return@mapNotNull null
                FriendOwedItem(
                    itemId = entry.item.id,
                    itemName = entry.item.name,
                    quantity = entry.item.quantity,
                    unitValueInCents = entry.item.valueInCents,
                    amountInCents = (entry.item.valueInCents * entry.item.quantity) / sharersCount
                )
            }
            .toList()
    }

    // --- Exportação ---
    fun onExportClicked() {
        val currentCheck = uiState.value.check ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // 1. Prepara os dados
                val exportData = prepareCheckExportDataUseCase(currentCheck.id)

                // 2. Gera o PDF
                val file = pdfExportManager.generateCheckPdf(exportData)

                // 3. Avisa a UI para compartilhar
                _uiEvent.emit(PaidCheckUiEvent.ShareFile(file))
            } catch (e: Exception) {
                Timber.e(e, "Erro ao gerar PDF: ${e.message}")
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}


