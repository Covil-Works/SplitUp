package com.thaicrew.splitup.check.ui

sealed interface CheckUiEvent {
    data class ShowSnackbar(val message: String) : CheckUiEvent
    object NavigateToCreateCheck : CheckUiEvent
}