package com.thaicrew.splitup.history.ui

import com.thaicrew.splitup.check.domain.Check
import com.thaicrew.splitup.check.domain.ItemWithSharers
import com.thaicrew.splitup.check.ui.CheckViewMode
import com.thaicrew.splitup.check.ui.FriendOwedItem
import com.thaicrew.splitup.friend.domain.Friend

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