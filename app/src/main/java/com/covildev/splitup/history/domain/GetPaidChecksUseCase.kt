package com.covildev.splitup.history.domain

import com.covildev.splitup.check.domain.Check
import com.covildev.splitup.check.domain.CheckRepository
import com.covildev.splitup.check.domain.CheckStatus
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/* Recupera todas as comandas pagas (HistÃ³rico) */
class GetPaidChecksUseCase @Inject constructor(
    private val repository: CheckRepository
) {
    operator fun invoke(): Flow<List<Check>> {
        return repository.getChecksByStatus(CheckStatus.PAID)
    }
}
