package com.thaicrew.splitup.check.domain

import kotlinx.coroutines.flow.Flow

/* Recupera os itens de uma comanda */

data class GetItemsByCheckUseCase(
    private val repository: ItemRepository
) {
    operator fun invoke(checkId: Int): Flow<List<Item>> {
        return repository.getItemsForCheck(checkId)
    }
}
