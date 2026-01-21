package com.thaicrew.splitup.check.ui

sealed interface CheckUiEvent {
    data class ShowSnackbar(val message: String) : CheckUiEvent
    data class NavigateToCheckDetail(val checkId: Int) : CheckUiEvent}