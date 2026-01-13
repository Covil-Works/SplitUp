package com.thaicrew.splitup.history.domain

import com.thaicrew.splitup.check.domain.Check
import com.thaicrew.splitup.check.domain.CheckRepository
import com.thaicrew.splitup.check.domain.CheckStatus
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/* Recupera todas as comandas pagas (Histórico) */
class GetPaidChecksUseCase @Inject constructor(
    private val repository: CheckRepository
) {
    operator fun invoke(): Flow<List<Check>> {
        return repository.getChecksByStatus(CheckStatus.PAID)
    }
}