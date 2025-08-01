package com.thaicrew.splitup.friend.domain

import android.util.Log

class SoftDeleteFriendUseCase(private val repository: FriendRepository) {

    suspend operator fun invoke(id: Int): SoftDeleteFriendUseCaseResult {
        val friendToDeactivate = repository.findFriendById(id)

        if (friendToDeactivate == null) {
            return SoftDeleteFriendUseCaseResult.FriendNotFound
        }

        if (!friendToDeactivate.isActive) {
            return SoftDeleteFriendUseCaseResult.AlreadyInactive
        }

        repository.deactivateFriend(id)
        return SoftDeleteFriendUseCaseResult.Success
    }
}

sealed interface SoftDeleteFriendUseCaseResult {
    data object Success : SoftDeleteFriendUseCaseResult
    data object FriendNotFound : SoftDeleteFriendUseCaseResult
    data object AlreadyInactive : SoftDeleteFriendUseCaseResult
}