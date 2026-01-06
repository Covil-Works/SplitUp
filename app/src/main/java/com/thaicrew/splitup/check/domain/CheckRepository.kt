package com.thaicrew.splitup.check.domain

import com.thaicrew.splitup.friend.domain.Friend
import kotlinx.coroutines.flow.Flow

interface CheckRepository {

    fun getAllChecks(): Flow<List<Check>>

    fun getChecksByStatus(status: CheckStatus): Flow<List<Check>>

    fun getCheckByIdFlow(id: Int): Flow<Check?>

    suspend fun getCheckById(id: Int): Check?

    suspend fun saveCheck(check: Check): Long

    suspend fun deleteCheck(check: Check)

    suspend fun addParticipant(checkId: Int, friendId: Int)

    suspend fun removeParticipant(checkId: Int, friendId: Int)

    fun getParticipants(checkId: Int): Flow<List<Friend>>

    suspend fun addParticipants(checkId: Int, friendIds: List<Int>)

}
