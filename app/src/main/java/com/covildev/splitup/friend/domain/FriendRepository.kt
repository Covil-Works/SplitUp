package com.covildev.splitup.friend.domain

import android.hardware.camera2.CameraExtensionSession.StillCaptureLatency
import com.covildev.splitup.friend.domain.Friend
import kotlinx.coroutines.flow.Flow

interface FriendRepository {
    fun getActiveFriends(): Flow<List<Friend>>

    suspend fun findFriendByName(name: String): Friend?

    suspend fun findFriendById(id: Int): Friend?

    suspend fun addFriend(friend: Friend)

    suspend fun updateFriend(friend: Friend)

    suspend fun isFriendActive(id: Int): Boolean

    suspend fun deactivateFriend(id: Int)

    suspend fun hardDeleteFriend(id: Int)

}
