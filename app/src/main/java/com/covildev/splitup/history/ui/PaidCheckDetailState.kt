package com.covildev.splitup.history.ui

import com.covildev.splitup.check.domain.Check
import com.covildev.splitup.check.domain.ItemWithSharers
import com.covildev.splitup.check.ui.CheckViewMode
import com.covildev.splitup.check.ui.FriendOwedItem
import com.covildev.splitup.friend.domain.Friend

data class PaidCheckDetailState(
    val isLoading: Boolean = true,
    val check: Check? = null,

    // Dados para visualização
    val itemsWithSharers: List<ItemWithSharers> = emptyList(),
    val participants: List<Friend> = emptyList(), // Necessário para a aba "Por Pessoa"

    // Totais calculados
    val checkTotal: Long = 0L,
    val friendTotals: Map<Int, Long> = emptyMap(),

    // Controle de Visualização
    val viewMode: CheckViewMode = CheckViewMode.ByItem,

    // Controle de Expansão (Apenas leitura)
    val expandedItemId: Int? = null,
    val expandedFriendId: Int? = null,
    val expandedFriendOwedItems: List<FriendOwedItem> = emptyList(),

    // Dialogs de Ação
    val showDeleteDialog: Boolean = false,
    val showReopenDialog: Boolean = false
)

