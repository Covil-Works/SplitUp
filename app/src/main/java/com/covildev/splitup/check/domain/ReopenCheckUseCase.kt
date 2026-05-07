package com.covildev.splitup.check.domain

import javax.inject.Inject

class ReopenCheckUseCase @Inject constructor(
    private val repository: CheckRepository
) {
    /**
     * Reabre uma comanda fechada.
     * Define o status como OPEN e remove a data de fechamento.
     */
    suspend operator fun invoke(check: Check) {

        val reopenedCheck = check.copy(
            status = CheckStatus.OPEN,
            closingDate = null
        )
        repository.saveCheck(reopenedCheck)
    }
}
