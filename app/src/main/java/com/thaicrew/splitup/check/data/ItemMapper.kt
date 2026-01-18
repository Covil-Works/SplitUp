package com.thaicrew.splitup.check.data

import com.thaicrew.splitup.check.domain.Item


fun ItemEntity.toDomain(): Item = Item(
    id = this.id,
    checkId = this.checkId,
    name = this.name,
    quantity = this.quantity,
    valueInCents = this.valueInCents
)

fun Item.toEntity(): ItemEntity = ItemEntity(
    id = this.id,
    checkId = this.checkId,
    name = this.name,
    quantity = this.quantity,
    valueInCents = this.valueInCents
)

fun List<ItemEntity>.toDomain(): List<Item> = map { it.toDomain() }