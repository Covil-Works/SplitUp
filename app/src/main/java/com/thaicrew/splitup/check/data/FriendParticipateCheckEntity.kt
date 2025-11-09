package com.thaicrew.splitup.check.data

import androidx.room.Entity
import androidx.room.ColumnInfo
import androidx.room.ForeignKey
import com.thaicrew.splitup.friend.data.FriendEntity

@Entity(
    tableName = "friend_partipate_checks",
    primaryKeys = ["friend_id", "check_id"],
    foreignKeys = [
        ForeignKey(
            entity = FriendEntity::class,
            parentColumns = ["friend_id"],
            childColumns = ["friend_id"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = CheckEntity::class,
            parentColumns = ["check_id"],
            childColumns = ["check_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)

data class FriendParticipateCheckEntity(

    @ColumnInfo(name = "friend_id", index = true)
    val friendId: Int,

    @ColumnInfo(name = "check_id", index = true)
    val checkId: Int,
)