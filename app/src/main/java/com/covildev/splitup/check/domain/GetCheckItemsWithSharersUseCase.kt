package com.covildev.splitup.check.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class GetCheckItemsWithSharersUseCase @Inject constructor(
    private val repository: ItemRepository
) {
    operator fun invoke(checkId: Int): Flow<List<ItemWithSharers>> {
        val itemsFlow = repository.getItemsForCheck(checkId)
        val sharesFlow = repository.getItemShares(checkId)

        // Combina os dois fluxos (Items e Shares) sempre que algum deles mudar
        return combine(itemsFlow, sharesFlow) { items, shares ->
            items.map { item ->
                // Para cada item, encontra quem divide
                val itemSharers = shares
                    .filter { share -> share.itemId == item.id }
                    .map { share -> share.friendId }

                ItemWithSharers(item, itemSharers)
            }
        }
    }
}
