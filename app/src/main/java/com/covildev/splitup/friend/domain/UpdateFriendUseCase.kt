package com.covildev.splitup.friend.domain

import timber.log.Timber
import javax.inject.Inject

class UpdateFriendUseCase @Inject constructor(
    private val repository: FriendRepository
) {
    suspend operator fun invoke(friendToUpdate: Friend, newName: String): UpdateFriendResult {
        Timber.i("Iniciando atualizaÃ§Ã£o para o amigo ID: ${friendToUpdate.id} com o novo nome: '$newName'")
        val trimmedName = newName.trim()

        if (trimmedName.isBlank()) {
            Timber.w("Falha na validaÃ§Ã£o: o novo nome estÃ¡ vazio ou contÃ©m apenas espaÃ§os.")
            return UpdateFriendResult.Error(InvalidFriendNameException("O nome nÃ£o pode ser vazio."))
        }

        val existingFriend = repository.findFriendByName(trimmedName)
        if (existingFriend != null && existingFriend.id != friendToUpdate.id) {
            Timber.w("Falha na validaÃ§Ã£o: o nome '$trimmedName' jÃ¡ estÃ¡ em uso por outro amigo (ID: ${existingFriend.id}).")
            return UpdateFriendResult.Error(FriendAlreadyExistsException("JÃ¡ existe um amigo com este nome."))
        }

        Timber.d("ValidaÃ§Ã£o bem-sucedida. Atualizando amigo no repositÃ³rio.")
        val updatedFriend = friendToUpdate.copy(name = trimmedName)
        repository.updateFriend(updatedFriend)
        return UpdateFriendResult.Success
    }
}

/**
 * Representa os possÃ­veis resultados da operaÃ§Ã£o de atualizaÃ§Ã£o.
 * Usar uma sealed interface torna o tratamento de resultados no ViewModel
 * mais seguro e explÃ­cito.
 */
sealed interface UpdateFriendResult {
    data object Success : UpdateFriendResult
    data class Error(val exception: Exception) : UpdateFriendResult
}
