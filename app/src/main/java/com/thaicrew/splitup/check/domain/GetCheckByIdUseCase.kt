package com.thaicrew.splitup.check.domain

/* Recupera uma comanda pelo ID */

data class GetCheckByIdUseCase(
    private val repository: CheckRepository
){
    suspend operator fun invoke(checkId: Int) : Check? {
        return repository.getCheckById(checkId)
    }
}
