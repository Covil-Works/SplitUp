package com.thaicrew.splitup.friend.data

import com.thaicrew.splitup.friend.domain.Friend

fun Friend.toEntity(): FriendEntity{
    return FriendEntity(
        id = this.id,
        name = this.name,
        isActive = this.isActive
    )
}

fun FriendEntity.toDomain(): Friend {
    return Friend(
        id = this.id,
        name = this.name,
        isActive = this.isActive
    )
}
