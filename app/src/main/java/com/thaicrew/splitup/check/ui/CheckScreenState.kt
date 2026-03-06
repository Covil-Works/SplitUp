package com.thaicrew.splitup.check.ui

import com.thaicrew.splitup.check.domain.Check

data class CheckScreenState(
    val checks: List<Check> = emptyList(),
    val isLoading: Boolean = true,
    val infoDialogMessage: String? = null
)