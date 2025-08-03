package com.thaicrew.splitup.friend.domain

import timber.log.Timber

class ReactivateAddFriendUseCase(
    private val repository: FriendRepository
) {
    suspend operator fun invoke(friend: Friend) {
        Timber.i("Tentando reativar o amigo: '${friend.name}' (ID: ${friend.id})")
        val friendToReactivate = friend.copy(isActive = true)
        repository.updateFriend(friendToReactivate)
    }
}