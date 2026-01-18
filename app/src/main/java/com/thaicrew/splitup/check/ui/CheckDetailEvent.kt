package com.thaicrew.splitup.check.ui

sealed interface CheckDetailUiEvent {
    data class ShowSnackbar(val message: String) : CheckDetailUiEvent
    object NavigateBack : CheckDetailUiEvent
}