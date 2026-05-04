package com.covildev.splitup.check.domain

import javax.inject.Inject

/* Deleta uma comanda */

data class DeleteCheckUseCase @Inject constructor(
    private val repository: CheckRepository
){
    suspend operator fun invoke(check: Check){
        return repository.deleteCheck(check)
    }
}

