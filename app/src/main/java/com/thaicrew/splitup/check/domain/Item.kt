package com.thaicrew.splitup.check.domain

data class Item(
    val id: Int = 0,
    val checkId: Int,
    val name: String,
    val quantity: Int,
    val valueInCents: Long
)