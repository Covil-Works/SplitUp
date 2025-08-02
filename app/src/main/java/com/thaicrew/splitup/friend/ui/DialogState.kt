package com.thaicrew.splitup.friend.ui

import com.thaicrew.splitup.friend.domain.Friend

sealed interface DialogState {
    object Hidden : DialogState
    data class ConfirmDeactivation(val friend: Friend) : DialogState
    data class ConfirmReactivation(val friend: Friend) : DialogState
    data class ShowEdit(val friend: Friend) : DialogState
}