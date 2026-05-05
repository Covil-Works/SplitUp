package com.covildev.splitup.friend.domain

import timber.log.Timber
import javax.inject.Inject

class SoftDeleteFriendUseCase @Inject constructor(
    private val repository: FriendRepository
) {
    suspend operator fun invoke(id: Int): SoftDeleteFriendUseCaseResult {
        Timber.i("Iniciando soft delete para o amigo com ID: $id")

        val friendToDeactivate = repository.findFriendById(id)

        if (friendToDeactivate == null) {
            Timber.w("Tentativa de desativar um amigo que não foi encontrado com o ID: $id.")
            return SoftDeleteFriendUseCaseResult.FriendNotFound
        }

        if (!friendToDeactivate.isActive) {
            Timber.d("Amigo '${friendToDeactivate.name}' já estava inativo. Nenhuma ação necessária.")
            return SoftDeleteFriendUseCaseResult.AlreadyInactive
        }

        Timber.d("Amigo '${friendToDeactivate.name}' encontrado e ativo. Procedendo com a desativação.")
        repository.deactivateFriend(id)
        return SoftDeleteFriendUseCaseResult.Success
    }
}

sealed interface SoftDeleteFriendUseCaseResult {
    data object Success : SoftDeleteFriendUseCaseResult
    data object FriendNotFound : SoftDeleteFriendUseCaseResult
    data object AlreadyInactive : SoftDeleteFriendUseCaseResult
}

