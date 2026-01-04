package com.thaicrew.splitup.check.domain

import javax.inject.Inject

/* Deleta uma comanda */

data class DeleteCheckUseCase @Inject constructor(
    private val repository: CheckRepository
){
    suspend fun invoke(check: Check){
        return repository.deleteCheck(check)
    }
}
