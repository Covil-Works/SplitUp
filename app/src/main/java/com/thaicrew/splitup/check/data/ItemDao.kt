package com.thaicrew.splitup.check.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ItemEntity): Long

    @Update
    suspend fun updateItem(item: ItemEntity)

    @Delete
    suspend fun deleteItem(item: ItemEntity)

    @Query("DELETE FROM friend_share_item_table WHERE item_id = :itemId AND friend_id = :friendId")
    suspend fun deleteItemShare(itemId: Int, friendId: Int)

    @Query("SELECT * FROM item_table WHERE check_id = :checkId")
    fun getItemsForCheck(checkId: Int): Flow<List<ItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItemShare(share: FriendShareItemEntity)

    @Query("SELECT * FROM friend_share_item_table WHERE check_id = :checkId")
    fun getItemSharesForCheck(checkId: Int): Flow<List<FriendShareItemEntity>>

    @Query("SELECT COUNT(*) FROM item_table WHERE check_id = :checkId")
    suspend fun countItemsForCheck(checkId: Int): Int
}