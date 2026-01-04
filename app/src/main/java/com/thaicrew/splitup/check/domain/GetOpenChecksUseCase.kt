package com.thaicrew.splitup.check.domain

import kotlinx.coroutines.flow.Flow

/* Recupera todas as comandas abertas */

data class GetOpenChecksUseCase(
    private val repository: CheckRepository
){
    operator fun invoke(): Flow<List<Check>>{
        return repository.getChecksByStatus(CheckStatus.OPEN)
    }
}
