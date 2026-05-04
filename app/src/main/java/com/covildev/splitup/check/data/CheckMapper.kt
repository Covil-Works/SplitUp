package com.covildev.splitup.check.data

import com.covildev.splitup.check.domain.Check
import com.covildev.splitup.check.domain.CheckStatus

fun CheckEntity.toDomain(): Check {
    return Check(
        id = this.id,
        name = this.name,
        creationDate = this.creationDate,
        closingDate = this.closingDate,
        status = CheckStatus.valueOf(this.status)
    )
}

fun Check.toEntity(): CheckEntity {
    return CheckEntity(
        id = this.id,
        name = this.name,
        creationDate = this.creationDate,
        closingDate = this.closingDate,
        status = this.status.name
    )
}


fun List<CheckEntity>.toDomain(): List<Check> {
    return this.map { it.toDomain() }
}
