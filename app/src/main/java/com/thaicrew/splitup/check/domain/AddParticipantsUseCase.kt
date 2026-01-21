package com.thaicrew.splitup.check.domain

import javax.inject.Inject

class AddParticipantsUseCase @Inject constructor(
    private val repository: CheckRepository
) {
    suspend operator fun invoke(checkId: Int, friendIds: List<Int>) {
        repository.addParticipants(checkId, friendIds)
    }
}