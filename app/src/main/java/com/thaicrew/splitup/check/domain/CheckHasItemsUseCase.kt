package com.thaicrew.splitup.check.domain

import javax.inject.Inject

data class CheckHasItemsUseCase @Inject constructor(
    private val repository: ItemRepository
) {
    suspend operator fun invoke(checkId: Int): Boolean {
        return repository.hasItems(checkId)
    }
}
