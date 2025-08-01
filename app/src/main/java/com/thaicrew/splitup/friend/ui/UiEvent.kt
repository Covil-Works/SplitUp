package com.thaicrew.splitup.friend.ui

sealed interface UiEvent {
    data class ShowSnackbar(val message: String) : UiEvent
}