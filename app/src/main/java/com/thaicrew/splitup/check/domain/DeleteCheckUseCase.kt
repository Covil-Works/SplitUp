package com.thaicrew.splitup.check.domain

/* Deleta uma comanda */

data class DeleteCheckUseCase(
    private val repository: CheckRepository
){
    suspend fun invoke(check: Check){
        return repository.deleteCheck(check)
    }
}
