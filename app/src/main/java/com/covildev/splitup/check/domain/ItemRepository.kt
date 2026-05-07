package com.covildev.splitup.check.domain

import kotlinx.coroutines.flow.Flow

interface ItemRepository {
    fun getItemsForCheck(checkId: Int): Flow<List<Item>>

    suspend fun saveItem(item: Item): Long

    suspend fun deleteItem(item: Item)

    suspend fun addItemShare(itemId: Int, friendId: Int, checkId: Int)

    suspend fun removeItemShare(itemId: Int, friendId: Int)

    fun getItemShares(checkId: Int): Flow<List<ItemShare>>

    suspend fun hasItems(checkId: Int): Boolean
}
