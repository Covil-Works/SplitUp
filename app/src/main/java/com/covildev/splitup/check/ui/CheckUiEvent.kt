package com.covildev.splitup.check.ui

sealed interface CheckUiEvent {
    data class NavigateToCheckDetail(val checkId: Int) : CheckUiEvent
}

