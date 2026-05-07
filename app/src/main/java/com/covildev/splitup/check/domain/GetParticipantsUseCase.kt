package com.covildev.splitup.check.domain

import javax.inject.Inject

class GetParticipantsUseCase @Inject constructor(
    private val repository: CheckRepository
) {
    operator fun invoke(checkId: Int) = repository.getParticipants(checkId)
}
