package com.covildev.splitup.common.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
// Friend
import com.covildev.splitup.friend.data.FriendEntity
import com.covildev.splitup.friend.data.FriendDao
// Check
import com.covildev.splitup.check.data.CheckEntity
import com.covildev.splitup.check.data.CheckDao
import com.covildev.splitup.check.data.FriendParticipateCheckEntity
import com.covildev.splitup.check.data.FriendShareItemEntity
// Item
import com.covildev.splitup.check.data.ItemEntity
import com.covildev.splitup.check.data.ItemDao

@Database(
    entities = [
        FriendEntity::class,
        ItemEntity::class,
        CheckEntity::class,
        FriendShareItemEntity::class,
        FriendParticipateCheckEntity::class
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
