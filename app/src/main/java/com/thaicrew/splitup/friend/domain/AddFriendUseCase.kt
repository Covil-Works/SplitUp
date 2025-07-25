package com.thaicrew.splitup.friend.domain

import android.util.Log

class AddFriendUseCase(private val repository: FriendRepository) {
    companion object {
        private val tag: String = "AddFriendUseCase"
    }

    suspend operator fun invoke(name: String){

        if (name.isBlank()){
            Log.i(tag, "Name is empty!")
            throw InvalidFriendNameException()
        }

        if (repository.findFriendByName(name.trim()) != null){
            Log.i(tag, "Already have this name!")
            throw FriendAlreadyExistsException()
        }

        val newFriend = Friend(name = name.trim(), isActive = true)

        repository.addFriend(newFriend)
    }
}

class InvalidFriendNameException : Exception("O nome do amigo não pode ser vazio.")
class FriendAlreadyExistsException : Exception("Um amigo com este nome já existe.")