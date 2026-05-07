package com.covildev.splitup.check.domain

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCheckByIdFlowUseCase @Inject constructor(
    private val repository: CheckRepository
) {
    operator fun invoke(checkId: Int): Flow<Check?> {
        return repository.getCheckByIdFlow(checkId)
    }
}
