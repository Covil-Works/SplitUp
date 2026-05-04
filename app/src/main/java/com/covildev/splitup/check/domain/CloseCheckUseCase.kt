package com.covildev.splitup.check.domain

import javax.inject.Inject

class CloseCheckUseCase @Inject constructor(
    private val repository: CheckRepository
) {
    suspend operator fun invoke(check: Check) {
        val closedCheck = check.copy(
            status = CheckStatus.PAID,
            closingDate = System.currentTimeMillis()
        )
        repository.saveCheck(closedCheck)
    }
}
