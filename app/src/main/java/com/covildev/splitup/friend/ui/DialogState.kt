package com.covildev.splitup.friend.ui

import com.covildev.splitup.friend.domain.Friend

sealed interface DialogState {
    object Hidden : DialogState
    data class ConfirmDeactivation(val friend: Friend) : DialogState
    data class ConfirmReactivation(val friend: Friend) : DialogState
    data class ShowEdit(val friend: Friend) : DialogState
    data class CannotDelete(val friend: Friend, val checkNames: List<String>) : DialogState
    data class ConfirmHardDelete(val friend: Friend) : DialogState
}
