package com.thaicrew.splitup.friend.data

import  com.thaicrew.splitup.friend.domain.Friend
import  com.thaicrew.splitup.friend.domain.FriendRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FriendRepositoryImpl(
    private val dao: FriendDao
) : FriendRepository {
    override fun getActiveFriends(): Flow<List<Friend>> {
        return  dao.getActiveFriends().map{
            friendEntities -> friendEntities.map {entity -> entity.toDomain()}
        }
    }

    override suspend fun findFriendByName(name: String): Friend? {
        return dao.findByName(name)?.toDomain()
    }

    override suspend fun findFriendById(id: Int): Friend? {
        return dao.findById(id)?.toDomain()
    }

    override suspend fun addFriend(friend: Friend) {
        dao.insert(friend.toEntity())
    }

    override suspend fun updateFriend(friend: Friend) {
        dao.update(friend.toEntity())
    }

    override suspend fun isFriendActive(id: Int): Boolean {
        return dao.isFriendActive(id)
    }

}