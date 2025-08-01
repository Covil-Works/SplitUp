package com.thaicrew.splitup.friend.domain

class ReactivateAddFriendUseCase(
    private val repository: FriendRepository
) {
    suspend operator fun invoke(friend: Friend) {
        val friendToReactivate = friend.copy(isActive = true)
        repository.updateFriend(friendToReactivate)
    }
}