package com.covildev.splitup.friend.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "friend")
data class FriendEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "friend_id")
    val id: Int = 0,

    @ColumnInfo(name = "friend_name")
    val name: String,

    @ColumnInfo(name = "friend_is_active", defaultValue = "1")
    val isActive: Boolean
)
