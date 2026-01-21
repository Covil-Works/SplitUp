package com.thaicrew.splitup.check.domain

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/* Cria uma nova comanda */
data class CreateCheckUseCase @Inject constructor(
    private val repository: CheckRepository
) {
    suspend operator fun invoke(name: String): Long {
        if (name.isBlank()){
            throw IllegalArgumentException("A comanda deve ter um nome.")
        }

        val newCheck = Check(
            id = 0,
            name = name.trim(),
            creationDate = System.currentTimeMillis(),
            closingDate = null,
            status = CheckStatus.OPEN
        )

        return repository.saveCheck(newCheck)
    }
}
