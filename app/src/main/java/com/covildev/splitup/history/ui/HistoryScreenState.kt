package com.covildev.splitup.history.ui

import com.covildev.splitup.check.domain.Check

data class HistoryScreenState(
    val checks: List<Check> = emptyList(),
    val isLoading: Boolean = true
)
