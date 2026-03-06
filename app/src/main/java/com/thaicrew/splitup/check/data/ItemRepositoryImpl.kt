package com.thaicrew.splitup.check.data

import com.thaicrew.splitup.check.domain.Item
import com.thaicrew.splitup.check.domain.ItemRepository
import com.thaicrew.splitup.check.domain.ItemShare
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
            // IMPORTANTE: não usar REPLACE em update, pois ele faz DELETE+INSERT e dispara CASCADE.
            itemDao.updateItem(entity)
            item.id.toLong()
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

    override fun getItemShares(checkId: Int): Flow<List<ItemShare>> {
        return itemDao.getItemSharesForCheck(checkId).map { list ->
            list.map { ItemShare(itemId = it.itemId, friendId = it.friendId) }
        }
    }

    override suspend fun hasItems(checkId: Int): Boolean {
        return itemDao.countItemsForCheck(checkId) > 0
    }
}