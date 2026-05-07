package com.covildev.splitup.check.domain

import javax.inject.Inject

/* Recebe a lista de itens e devolve o total em cêntimos (Long)*/

class CalculateCheckTotalUseCase @Inject constructor(){
    operator fun invoke(items: List<Item>): Long {
        return items.sumOf { item ->
            item.valueInCents * item.quantity
        }
    }
}

