package com.thaicrew.splitup.common.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
// Friend
import com.thaicrew.splitup.friend.data.FriendEntity
import com.thaicrew.splitup.friend.data.FriendDao
// Check
import com.thaicrew.splitup.check.data.CheckEntity
import com.thaicrew.splitup.check.data.CheckDao
import com.thaicrew.splitup.check.data.FriendShareItemEntity
// Item
import com.thaicrew.splitup.check.data.ItemEntity
import com.thaicrew.splitup.check.data.ItemDao

@Database(
    entities = [
        FriendEntity::class,
        ItemEntity::class,
        CheckEntity::class,
        FriendShareItemEntity::class
        // New tables go here

    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun friendDao(): FriendDao
    abstract fun checkDao(): CheckDao
    abstract fun itemDao(): ItemDao
}