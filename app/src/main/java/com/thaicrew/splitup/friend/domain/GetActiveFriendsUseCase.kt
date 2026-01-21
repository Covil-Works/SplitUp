package com.thaicrew.splitup.friend.domain

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetActiveFriendsUseCase @Inject constructor(
    private val repository: FriendRepository
) {
    operator fun invoke(): Flow<List<Friend>> {
        return repository.getActiveFriends()
    }
}