package com.thaicrew.splitup.common.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
// Friend
import com.thaicrew.splitup.friend.data.FriendEntity
import com.thaicrew.splitup.friend.data.FriendDao

@Database(
    entities = [
        FriendEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun friendDao(): FriendDao
}