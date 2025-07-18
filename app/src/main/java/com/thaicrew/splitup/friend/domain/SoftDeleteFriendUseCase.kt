package com.thaicrew.splitup.friend.domain

import android.util.Log

class DeactivateFriendUseCase(private val repository: FriendRepository) {

    companion object {
        private const val TAG = "DeactivateFriendUseCase"
    }

    suspend operator fun invoke(id: Int) {

        val friendToDeactivate = repository.findFriendById(id)

        if (friendToDeactivate == null) {
            Log.w(TAG, "Attempted to deactivate a friend that does not exist. ID: $id")
            throw FriendNotFoundException("O amigo com o ID $id não foi encontrado.")
        }

        if (!friendToDeactivate.isActive) {
            Log.w(TAG, "Attempted to deactivate an already inactive friend. ID: $id")
            throw FriendAlreadyInactiveException("Este amigo já está desativado.")
        }

        repository.deactivateFriend(id)
        Log.i(TAG, "Friend with ID $id successfully deactivated.")
    }
}

class FriendNotFoundException(override val message: String) : Exception(message)
class FriendAlreadyInactiveException(override val message: String) : Exception(message)