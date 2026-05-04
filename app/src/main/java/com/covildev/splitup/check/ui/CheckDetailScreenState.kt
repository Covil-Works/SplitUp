package com.covildev.splitup.check.ui

import com.covildev.splitup.check.domain.Check
import com.covildev.splitup.check.domain.Item
import com.covildev.splitup.friend.domain.Friend
import com.covildev.splitup.check.domain.ItemWithSharers

data class CheckDetailScreenState(
    val check: Check? = null,
    val participants: List<Friend> = emptyList(), // friends of check
    val availableFriends: List<Friend> = emptyList(), // friends for Bottom Sheet
    val items: List<Item> = emptyList(), // Lista de itens jÃ¡ adicionados
    val isLoading: Boolean = true,
    val isError: Boolean = false,
    val showBottomSheet: Boolean = false,

    // Para Adicionar Item
    val newItemName: String = "",
    val newItemQuantity: Int = 1,
    val newItemValue: String = "",
    val selectedFriendIdsForItem: Set<Int> = emptySet(), // IDs dos amigos que dividem
    val isAllSelected: Boolean = true, // para comeÃ§ar marcado como "Todos"

    // Para visualizaÃ§Ã£o
    val itemsWithSharers: List<ItemWithSharers> = emptyList(), // Lista rica para a UI
    val friendTotals: Map<Int, Long> = emptyMap(), // ID do Amigo -> Total a pagar em Cents
    val viewMode: CheckViewMode = CheckViewMode.ByItem, // Controle das abas

    // EdiÃ§Ã£o de Item
    val expandedItemId: Int? = null, // Qual item estÃ¡ aberto para ver detalhes
    val isEditing: Boolean = false,  // Se o item aberto estÃ¡ em modo de ediÃ§Ã£o
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
 * amountInCents jÃ¡ considera quantidade * valor unitÃ¡rio e a divisÃ£o entre os participantes do item.
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

