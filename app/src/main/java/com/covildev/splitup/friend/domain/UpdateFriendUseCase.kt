package com.covildev.splitup.friend.domain

import timber.log.Timber
import javax.inject.Inject

class UpdateFriendUseCase @Inject constructor(
    private val repository: FriendRepository
) {
    suspend operator fun invoke(friendToUpdate: Friend, newName: String): UpdateFriendResult {
        Timber.i("Iniciando atualização para o amigo ID: ${friendToUpdate.id} com o novo nome: '$newName'")
        val trimmedName = newName.trim()

        if (trimmedName.isBlank()) {
            Timber.w("Falha na validação: o novo nome está vazio ou contém apenas espaços.")
            return UpdateFriendResult.Error(InvalidFriendNameException("O nome não pode ser vazio."))
        }

        val existingFriend = repository.findFriendByName(trimmedName)
        if (existingFriend != null && existingFriend.id != friendToUpdate.id) {
            Timber.w("Falha na validação: o nome '$trimmedName' já está em uso por outro amigo (ID: ${existingFriend.id}).")
            return UpdateFriendResult.Error(FriendAlreadyExistsException("Já existe um amigo com este nome."))
        }

        Timber.d("Validação bem-sucedida. Atualizando amigo no repositório.")
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

