package com.thaicrew.splitup.check.data

import com.thaicrew.splitup.check.domain.Item
import com.thaicrew.splitup.check.domain.ItemRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ItemRepositoryImpl @Inject constructor(
    private val itemDao: ItemDao
) : ItemRepository {

    override fun getItemsForCheck(checkId: Int): Flow<List<Item>> {
        return itemDao.getItemsForCheck(checkId).map { it.toDomain() }
    }

    override suspend fun saveItem(item: Item): Long {
        val entity = item.toEntity()
        return if (item.id == 0) {
            itemDao.insertItem(entity)
        } else {
            // Como o insertItem usa OnConflictStrategy.REPLACE,
            // ele também serve como update se o ID já existir.
            itemDao.insertItem(entity)
        }
    }

    override suspend fun deleteItem(item: Item) {
        itemDao.deleteItem(item.toEntity())
    }

    override suspend fun addItemShare(itemId: Int, friendId: Int, checkId: Int) {
        val share = FriendShareItemEntity(
            friendId = friendId,
            itemId = itemId,
            checkId = checkId
        )
        itemDao.insertItemShare(share)
    }

    override suspend fun removeItemShare(itemId: Int, friendId: Int) {
        itemDao.deleteItemShare(itemId, friendId)
    }
}