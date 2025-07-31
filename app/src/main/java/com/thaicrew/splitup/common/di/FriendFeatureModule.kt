package com.thaicrew.splitup.common.di

import com.thaicrew.splitup.friend.data.FriendDao
import com.thaicrew.splitup.friend.data.FriendRepositoryImpl
import com.thaicrew.splitup.friend.domain.AddFriendUseCase
import com.thaicrew.splitup.friend.domain.FriendRepository
import com.thaicrew.splitup.friend.domain.GetActiveFriendsUseCase
import com.thaicrew.splitup.friend.domain.SoftDeleteFriendUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FriendFeatureModule {

    @Provides
    @Singleton
    fun provideFriendRepository(friendDao: FriendDao): FriendRepository {
        return FriendRepositoryImpl(friendDao)
    }

    @Provides
    fun provideAddFriendUseCase(repository: FriendRepository): AddFriendUseCase {
        return AddFriendUseCase(repository)
    }

    @Provides
    fun provideGetActiveFriendsUseCase(repository: FriendRepository): GetActiveFriendsUseCase {
        return GetActiveFriendsUseCase(repository)
    }

    @Provides
    fun provideSoftDeleteFriendsUseCase(repository: FriendRepository): SoftDeleteFriendUseCase {
        return SoftDeleteFriendUseCase(repository)
    }
}