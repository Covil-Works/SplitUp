package com.covildev.splitup.check.domain

import javax.inject.Inject

class GetOpenChecksForFriendUseCase @Inject constructor(
    private val repository: CheckRepository
) {
    suspend operator fun invoke(friendId: Int): List<Check> {
        return repository.getOpenChecksForFriend(friendId)
    }
}
