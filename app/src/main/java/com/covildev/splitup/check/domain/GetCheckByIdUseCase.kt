package com.covildev.splitup.check.domain

import javax.inject.Inject

/* Recupera uma comanda pelo ID */

data class GetCheckByIdUseCase @Inject constructor(
    private val repository: CheckRepository
){
    suspend operator fun invoke(checkId: Int) : Check? {
        return repository.getCheckById(checkId)
    }
}

