package com.covildev.splitup.check.domain

import javax.inject.Inject

/* Alterna o compartilhamento de um item */

data class ToggleItemShareUseCase @Inject constructor(
    private val repository: ItemRepository
) {
    suspend operator fun invoke(
        itemId: Int,
        friendId: Int,
        checkId: Int,
        isShared: Boolean
    ) {
        if (isShared) {
            repository.addItemShare(itemId, friendId, checkId)
        } else {
            repository.removeItemShare(itemId, friendId)
        }
    }
}

