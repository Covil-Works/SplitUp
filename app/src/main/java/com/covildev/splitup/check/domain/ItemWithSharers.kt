package com.covildev.splitup.check.domain

data class ItemWithSharers(
    val item: Item,
    val sharersIds: List<Int> // amigos
)
