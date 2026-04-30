package com.thaicrew.splitup.check.ui

import com.thaicrew.splitup.check.domain.Check

data class CheckScreenState(
    val checks: List<Check> = emptyList(),
    val isLoading: Boolean = true,
    val infoDialogMessage: String? = null,
    val participantsByCheckId: Map<Int, List<String>> = emptyMap(),
    val totalByCheckId: Map<Int, Long> = emptyMap()
)
