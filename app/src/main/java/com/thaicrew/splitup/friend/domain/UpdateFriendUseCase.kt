package com.thaicrew.splitup.friend.domain

class UpdateFriendUseCase (private val repository: FriendRepository) {

    suspend operator fun invoke(friendToUpdate: Friend, newName: String): UpdateFriendResult {
        val trimmedName = newName.trim()

        if (trimmedName.isBlank()) {
            return UpdateFriendResult.Error(InvalidFriendNameException("O nome não pode ser vazio."))
        }

        val existingFriend = repository.findFriendByName(trimmedName)
        if (existingFriend != null && existingFriend.id != friendToUpdate.id) {
            return UpdateFriendResult.Error(FriendAlreadyExistsException("Já existe um amigo com este nome."))
        }

        val updatedFriend = friendToUpdate.copy(name = trimmedName)
        repository.updateFriend(updatedFriend)
        return UpdateFriendResult.Success
    }
}

/**
 * Representa os possíveis resultados da operação de atualização.
 * Usar uma sealed interface torna o tratamento de resultados no ViewModel
 * mais seguro e explícito.
 */
sealed interface UpdateFriendResult {
    data object Success : UpdateFriendResult
    data class Error(val exception: Exception) : UpdateFriendResult
}
