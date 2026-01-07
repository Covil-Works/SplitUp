package com.thaicrew.splitup.check.domain

import javax.inject.Inject

class DeleteItemUseCase @Inject constructor(
    private val repository: ItemRepository
) {
    suspend operator fun invoke(item: Item) {
        repository.deleteItem(item)
    }
}