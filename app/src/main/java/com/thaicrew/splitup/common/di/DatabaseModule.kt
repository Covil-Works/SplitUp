package com.thaicrew.splitup.common.di

import android.content.Context
import androidx.room.Room
import com.thaicrew.splitup.common.data.local.AppDatabase
import com.thaicrew.splitup.friend.data.FriendDao
import com.thaicrew.splitup.check.data.ItemDao
import com.thaicrew.splitup.check.data.CheckDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "splitup.db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideFriendDao(appDatabase: AppDatabase): FriendDao {
        return appDatabase.friendDao()
    }

    @Provides
    fun provideCheckDao(appDatabase: AppDatabase) = appDatabase.checkDao()

    @Provides
    fun provideItemDao(appDatabase: AppDatabase) = appDatabase.itemDao()
}