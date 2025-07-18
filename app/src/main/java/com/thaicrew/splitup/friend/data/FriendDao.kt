package com.thaicrew.splitup.friend.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FriendDao {
    @Insert
    suspend fun insert(friend: FriendEntity): Long

    @Update
    suspend fun update(friend: FriendEntity): Int

    @Query("SELECT * from friend WHERE friend_is_active = 1 ORDER BY friend_name ASC")
    fun getActiveFriends(): Flow<List<FriendEntity>>

    @Query("SELECT * FROM friend WHERE friend_name = :name LIMIT 1")
    suspend fun findByName(name: String): FriendEntity?

    @Query("SELECT * FROM friend WHERE friend_id = :id")
    suspend fun findById(id: Int): FriendEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM friend WHERE friend_id = :id AND friend_is_active = 1 LIMIT 1)")
    suspend fun isFriendActive(id: Int): Boolean
}
