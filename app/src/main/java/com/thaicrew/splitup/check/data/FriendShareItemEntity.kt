package com.thaicrew.splitup.check.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import com.thaicrew.splitup.friend.data.FriendEntity

@Entity(
    tableName = "friend_share_item_table",
    primaryKeys = ["item_id", "friend_id"],
    foreignKeys = [
        ForeignKey(
            entity = FriendParticipateCheckEntity::class,
            parentColumns = ["friend_id", "check_id"],
            childColumns = ["friend_id", "check_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ItemEntity::class,
            parentColumns = ["item_id", "check_id"],
            childColumns = ["item_id", "check_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class FriendShareItemEntity(

    @ColumnInfo(name = "friend_id", index = true)
    val friendId: Int,

    @ColumnInfo(name = "item_id", index = true)
    val itemId: Int,

    @ColumnInfo(name = "check_id", index = true)
    val checkId: Int,
)
