package com.thaicrew.splitup.check.domain

import javax.inject.Inject

class RemoveParticipantUseCase @Inject constructor(
    private val repository: CheckRepository
) {
    suspend operator fun invoke(checkId: Int, friendId: Int) {
        repository.removeParticipant(checkId, friendId)
    }
}