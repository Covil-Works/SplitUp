package com.thaicrew.splitup.check.ui

import com.thaicrew.splitup.check.domain.Check
import com.thaicrew.splitup.check.domain.Item
import com.thaicrew.splitup.friend.domain.Friend
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.material.icons.filled.Remove

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
    val selectedFriendIdsForItem: Set<Int> = emptySet() // IDs dos amigos que dividem
)