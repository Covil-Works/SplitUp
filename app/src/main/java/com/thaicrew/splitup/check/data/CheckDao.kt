package com.thaicrew.splitup.check.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.thaicrew.splitup.friend.data.FriendEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CheckDao{
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheck(check: CheckEntity): Long

    @Update
    suspend fun updateCheck(check: CheckEntity)

    @Delete
    suspend fun deleteCheck(check: CheckEntity)

    @Query("SELECT * FROM check_table ORDER BY check_creation_date DESC")
    fun getAllChecks(): Flow<List<CheckEntity>>

    @Query("SELECT * FROM check_table WHERE check_status = :statusString ORDER BY check_creation_date DESC")
    fun getChecksByStatus(statusString: String): Flow<List<CheckEntity>>

    @Query("SELECT * FROM check_table WHERE check_id = :id")
    suspend fun getCheckById(id: Int): CheckEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertParticipant(join: FriendParticipateCheckEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertParticipants(participants: List<FriendParticipateCheckEntity>)

    @Query("DELETE FROM friend_participate_checks WHERE check_id = :checkId AND friend_id = :friendId")
    suspend fun removeParticipant(checkId: Int, friendId: Int)

    @Query("SELECT * FROM check_table WHERE check_id = :id")
    fun getCheckByIdFlow(id: Int): Flow<CheckEntity?>

    @Query("""
        SELECT friend.* FROM friend 
        INNER JOIN friend_participate_checks ON friend.friend_id = friend_participate_checks.friend_id 
        WHERE friend_participate_checks.check_id = :checkId
    """)
    fun getParticipantsByCheckId(checkId: Int): Flow<List<FriendEntity>>

    @Query("""
        SELECT check_table.* FROM check_table
        INNER JOIN friend_participate_checks ON check_table.check_id = friend_participate_checks.check_id
        WHERE friend_participate_checks.friend_id = :friendId AND check_table.check_status = 'OPEN'
    """)
    suspend fun getOpenChecksForFriend(friendId: Int): List<CheckEntity>

    @Query("SELECT COUNT(*) FROM friend_participate_checks WHERE friend_id = :friendId")
    suspend fun countParticipations(friendId: Int): Int
}

