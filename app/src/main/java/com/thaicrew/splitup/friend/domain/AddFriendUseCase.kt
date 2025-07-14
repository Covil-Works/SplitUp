package com.thaicrew.splitup.friend.domain

class AddFriendUseCase(private val repository: FriendRepository) {
    suspend operator fun invoke(name: String){
        if (name.isBlank()){
            throw InvalidFriendNameException()
        }

        if (repository.findFriendByName(name) != null){
            throw FriendAlreadyExistsException()
        }

        val mewFriend = Friend(name = name.trim(), isActive = true)
    }
}

class InvalidFriendNameException : Exception("O nome do amigo não pode ser vazio.")
class FriendAlreadyExistsException : Exception("Um amigo com este nome já existe.")
