package com.covildev.splitup.friend.domain

import timber.log.Timber
import javax.inject.Inject

class AddFriendUseCase @Inject constructor(private val repository: FriendRepository) {

    suspend operator fun invoke(name: String): AddFriendResult {
        val trimmedName = name.trim()
        Timber.i("Tentando adicionar amigo com o nome: '$trimmedName'")

        if (trimmedName.isBlank()) {
            Timber.w("Tentativa de adicionar amigo com nome vazio.")
            return AddFriendResult.Error(InvalidFriendNameException("O nome do amigo não pode ser vazio."))
        }

        return try {
            val existingFriend = repository.findFriendByName(trimmedName)

            if (existingFriend == null) {
                Timber.d("Nenhum amigo existente encontrado. Criando novo.")
                val newFriend = Friend(name = trimmedName, isActive = true)
                repository.addFriend(newFriend)
                Timber.i("Amigo '$trimmedName' adicionado com sucesso.")
                AddFriendResult.Success
            } else if (existingFriend.isActive) {
                Timber.d("Amigo '${existingFriend.name}' já existe e está ativo.")
                AddFriendResult.AlreadyExistsActive(existingFriend)
            } else {
                Timber.d("Amigo '${existingFriend.name}' já existe, mas está inativo. Sugerindo reativação.")
                AddFriendResult.NeedsReactivation(existingFriend)
            }
        } catch (e: Exception) {
            Timber.e(e, "Erro ao tentar adicionar o amigo '$trimmedName'")
            AddFriendResult.Error(e)
        }
    }
}

sealed class AddFriendResult {
    data object Success : AddFriendResult()
    data class AlreadyExistsActive(val friend: Friend) : AddFriendResult()
    data class NeedsReactivation(val friend: Friend) : AddFriendResult()
    data class Error(val exception: Exception) : AddFriendResult()
}

