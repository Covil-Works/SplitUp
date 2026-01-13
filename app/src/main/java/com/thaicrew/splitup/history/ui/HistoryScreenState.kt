package com.thaicrew.splitup.history.ui

import com.thaicrew.splitup.check.domain.Check

data class HistoryScreenState(
    val checks: List<Check> = emptyList(),
    val isLoading: Boolean = true
)