package com.thaicrew.splitup.check.domain

/* Recebe a lista de itens e devolve o total em cêntimos (Long)*/

class CalculateCheckTotalUseCase{
    operator fun invoke(items: List<Item>): Long {
        return items.sumOf { item ->
            item.valueInCents * item.quantity
        }
    }
}