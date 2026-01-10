package com.thaicrew.splitup.check.ui

import com.thaicrew.splitup.check.domain.Check
import com.thaicrew.splitup.check.domain.Item
import com.thaicrew.splitup.friend.domain.Friend
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.material.icons.filled.Remove
import com.thaicrew.splitup.check.domain.ItemWithSharers

data class CheckDetailScreenState(
    val check: Check? = null,
    val participants: List<Friend> = emptyList(), // friends of check
    val availableFriends: List<Friend> = emptyList(), // friends for Bottom Sheet
    val items: List<Item> = emptyList(), // Lista de itens já adicionados
    val isLoading: Boolean = true,
    val isError: Boolean = false,
    val showBottomSheet: Boolean = false,

    // Para Adicionar Item
    val newItemName: String = "",
    val newItemQuantity: Int = 1,
    val newItemValue: String = "",
    val selectedFriendIdsForItem: Set<Int> = emptySet(), // IDs dos amigos que dividem
    val isAllSelected: Boolean = true, // para começar marcado como "Todos"

    // Para visualização
    val itemsWithSharers: List<ItemWithSharers> = emptyList(), // Lista rica para a UI
    val friendTotals: Map<Int, Long> = emptyMap(), // ID do Amigo -> Total a pagar em Cents
    val viewMode: CheckViewMode = CheckViewMode.ByItem, // Controle das abas

    // Edição de Item
    val editingItemId: Int? = null, // relativo ao item aberto (null = nenhum)
    val editingName: String = "",
    val editingQuantity: Int = 1,
    val editingValue: String = "",
    val editingSharers: Set<Int> = emptySet(),

    // Fechamento da comanda
    val showCloseCheckDialog: Boolean = false
)

enum class CheckViewMode {
    ByItem,
    ByFriend
}