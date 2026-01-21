package com.thaicrew.splitup.check.domain

import timber.log.Timber
import javax.inject.Inject

class UpdateNameCheckUseCase @Inject constructor(
    private val repository: CheckRepository
) {
    suspend operator fun invoke(check: Check, newName: String): UpdateCheckResult {
        Timber.i("Iniciando atualização da comanda ID: ${check.id} para o nome: '$newName'")
        val trimmedName = newName.trim()

        if (trimmedName.isBlank()) {
            Timber.w("Falha na validação: o novo nome da comanda está vazio.")
            return UpdateCheckResult.Error("O nome da comanda não pode estar vazio.")
        }

        return try {
            val updatedCheck = check.copy(name = trimmedName)
            repository.saveCheck(updatedCheck)
            Timber.d("Comanda ID ${check.id} atualizada com sucesso.")
            UpdateCheckResult.Success
        } catch (e: Exception) {
            Timber.e(e, "Erro ao tentar atualizar a comanda ID ${check.id}")
            UpdateCheckResult.Error(e.message ?: "Erro desconhecido ao atualizar.")
        }
    }
}

/*
 Interface selada para representar os resultados da operação, garantindo um tratamento seguro na ViewModel.
*/
sealed interface UpdateCheckResult {
    data object Success : UpdateCheckResult
    data class Error(val message: String) : UpdateCheckResult
}