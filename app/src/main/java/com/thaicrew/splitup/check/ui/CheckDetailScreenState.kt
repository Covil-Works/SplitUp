package com.thaicrew.splitup.check.ui

import com.thaicrew.splitup.check.domain.Check
import com.thaicrew.splitup.friend.domain.Friend

data class CheckDetailScreenState(
    val check: Check? = null,
    val participants: List<Friend> = emptyList(), // friends of check
    val availableFriends: List<Friend> = emptyList(), // friends for Bottom Sheet
    val isLoading: Boolean = true,
    val isError: Boolean = false,
    val showBottomSheet: Boolean = false
)