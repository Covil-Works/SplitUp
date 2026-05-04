package com.covildev.splitup.friend.domain

import javax.inject.Inject

class HardDeleteFriendUseCase @Inject constructor(
    private val repository: FriendRepository
) {
    suspend operator fun invoke(friendId: Int) {
        repository.hardDeleteFriend(friendId)
    }
}
