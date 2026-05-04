package com.covildev.splitup.common.di
import com.covildev.splitup.check.data.CheckRepositoryImpl
import com.covildev.splitup.check.data.ItemRepositoryImpl
import com.covildev.splitup.check.domain.CheckRepository
import com.covildev.splitup.check.domain.ItemRepository
import com.covildev.splitup.friend.data.FriendRepositoryImpl
import com.covildev.splitup.friend.domain.FriendRepository
import dagger.Module
import dagger.Binds
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindFriendRepository(impl: FriendRepositoryImpl): FriendRepository

    @Binds
    @Singleton
    abstract fun bindCheckRepository(impl: CheckRepositoryImpl): CheckRepository

    @Binds
    @Singleton
    abstract fun bindItemRepository(impl: ItemRepositoryImpl): ItemRepository
}
