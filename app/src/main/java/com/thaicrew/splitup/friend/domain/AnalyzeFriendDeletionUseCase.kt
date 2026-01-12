package com.thaicrew.splitup.friend.domain

import com.thaicrew.splitup.check.domain.CheckRepository
import javax.inject.Inject

class AnalyzeFriendDeletionUseCase @Inject constructor(
    private val checkRepository: CheckRepository
) {
    suspend operator fun invoke(friendId: Int): FriendDeletionStatus {
        // 1. Verifica impeditivos (Comandas Abertas)
        val openChecks = checkRepository.getOpenChecksForFriend(friendId)

        if (openChecks.isNotEmpty()) {
            return FriendDeletionStatus.Blocked(openChecks.map { it.name })
        }

        // 2. Verifica histórico (Qualquer participação) de forma eficiente
        val hasHistory = checkRepository.hasAnyParticipation(friendId)

        return if (hasHistory) {
            FriendDeletionStatus.RequiresSoftDelete
        } else {
            FriendDeletionStatus.SafeHardDelete
        }
    }
}

sealed interface FriendDeletionStatus {
    data class Blocked(val checkNames: List<String>) : FriendDeletionStatus
    object RequiresSoftDelete : FriendDeletionStatus
    object SafeHardDelete : FriendDeletionStatus
}