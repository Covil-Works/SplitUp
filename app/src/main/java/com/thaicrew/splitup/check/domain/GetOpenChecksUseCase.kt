package com.thaicrew.splitup.check.domain

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/* Recupera todas as comandas abertas */

data class GetOpenChecksUseCase @Inject constructor(
    private val repository: CheckRepository
){
    operator fun invoke(): Flow<List<Check>>{
        return repository.getChecksByStatus(CheckStatus.OPEN)
    }
}
