package com.thaicrew.splitup.friend.domain

import android.util.Log
class AddFriendUseCase(private val repository: FriendRepository) {

    suspend operator fun invoke(name: String): AddFriendResult {
        val trimmedName = name.trim()

        if (trimmedName.isBlank()) {
            return AddFriendResult.Error(InvalidFriendNameException("O nome do amigo não pode ser vazio."))
        }

        val existingFriend = repository.findFriendByName(trimmedName)

        return if (existingFriend == null) {

            val newFriend = Friend(name = trimmedName, isActive = true)
            repository.addFriend(newFriend)
            AddFriendResult.Success

        } else if (existingFriend.isActive) {
            AddFriendResult.AlreadyExistsActive(existingFriend)

        } else {
            AddFriendResult.NeedsReactivation(existingFriend)
        }
    }
}

sealed class AddFriendResult {
    data object Success : AddFriendResult()
    data class AlreadyExistsActive(val friend: Friend) : AddFriendResult()
    data class NeedsReactivation(val friend: Friend) : AddFriendResult()
    data class Error(val exception: Exception) : AddFriendResult()
}