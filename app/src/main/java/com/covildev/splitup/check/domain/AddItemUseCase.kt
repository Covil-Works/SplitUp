package com.covildev.splitup.check.domain

import javax.inject.Inject

/* Adiciona um item à comanda */

data class AddItemUseCase @Inject constructor(
    private val repository: ItemRepository
) {
    suspend operator fun invoke(
        chekId: Int,
        itemName: String,
        itemQuantity: Int,
        itemValueInCents: Long
    ) : Long {
        if (itemName.isBlank()) throw IllegalArgumentException("O item deve ter um nome válido.")
        if (itemQuantity <= 0) throw IllegalArgumentException("A quantidade do item deve ser maior que zero.")
        if (itemValueInCents < 0) throw IllegalArgumentException("O valor do item não pode ser negativo.")

        val newItem = Item(
            id = 0,
            checkId = chekId,
            name = itemName.trim(),
            quantity = itemQuantity,
            valueInCents = itemValueInCents
        )
        return repository.saveItem(newItem)
    }
}


