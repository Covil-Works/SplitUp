package com.covildev.splitup.check.domain

import javax.inject.Inject

class UpdateItemUseCase @Inject constructor(
    private val repository: ItemRepository
) {
    suspend operator fun invoke(item: Item) {
        repository.saveItem(item)
    }
}
