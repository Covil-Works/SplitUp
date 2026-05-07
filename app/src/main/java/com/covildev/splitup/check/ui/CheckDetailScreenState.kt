package com.covildev.splitup.check.ui

import com.covildev.splitup.check.domain.Check
import com.covildev.splitup.check.domain.Item
import com.covildev.splitup.friend.domain.Friend
import com.covildev.splitup.check.domain.ItemWithSharers

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
    val expandedItemId: Int? = null, // Qual item está aberto para ver detalhes
    val isEditing: Boolean = false,  // Se o item aberto está em modo de edição
    val editingItemId: Int? = null, // relativo ao item aberto (null = nenhum)
    val editingName: String = "",
    val editingQuantity: Int = 1,
    val editingValue: String = "",
    val editingSharers: Set<Int> = emptySet(),
    val originalEditingSharers: Set<Int> = emptySet(),

    // Detalhe por amigo (ao expandir um card do amigo na aba "Por amigo")
    val expandedFriendId: Int? = null,
    val expandedFriendOwedItems: List<FriendOwedItem> = emptyList(),

    // Final da comanda
    val showCloseCheckDialog: Boolean = false,
    val checkTotal: Long = 0L,
    val showEditNameDialog: Boolean = false,

    // Dialog informativo
    val infoDialogMessage: String? = null
)

/**
 * Representa um item (ou parte dele) que um amigo deve pagar.
 * amountInCents já considera quantidade * valor unitário e a divisão entre os participantes do item.
 */
data class FriendOwedItem(
    val itemId: Int,
    val itemName: String,
    val quantity: Int,
    val unitValueInCents: Long,
    val amountInCents: Long
)

enum class CheckViewMode {
    ByItem,
    ByFriend
}


