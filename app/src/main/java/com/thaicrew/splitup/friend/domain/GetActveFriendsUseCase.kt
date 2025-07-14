package com.thaicrew.splitup.friend.domain

import kotlinx.coroutines.flow.Flow

class GetActveFriendsUseCase(
    private val repository: FriendRepository){
    operator fun invoke(): Flow<List<Friend>>{
        return repository.getActiveFriends()
    }
}