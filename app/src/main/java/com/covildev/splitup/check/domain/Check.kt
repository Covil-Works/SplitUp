package com.covildev.splitup.check.domain

enum class CheckStatus {
    OPEN,
    PENDING,
    PAID
}

data class Check(
    val id: Int = 0,
    val name: String,
    val creationDate: Long,
    val closingDate: Long?,
    val status: CheckStatus = CheckStatus.OPEN
)

