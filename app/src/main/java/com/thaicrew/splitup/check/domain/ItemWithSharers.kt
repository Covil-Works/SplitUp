package com.thaicrew.splitup.check.domain

data class ItemWithSharers(
    val item: Item,
    val sharersIds: List<Int> // amigos
)