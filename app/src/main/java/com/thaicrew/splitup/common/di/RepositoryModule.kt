package com.thaicrew.splitup.common.di
import com.thaicrew.splitup.check.data.CheckRepositoryImpl
import com.thaicrew.splitup.check.data.ItemRepositoryImpl
import com.thaicrew.splitup.check.domain.CheckRepository
import com.thaicrew.splitup.check.domain.ItemRepository
import com.thaicrew.splitup.friend.data.FriendRepositoryImpl
import com.thaicrew.splitup.friend.domain.FriendRepository
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