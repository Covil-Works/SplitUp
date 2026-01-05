package com.thaicrew.splitup.check.ui

import com.thaicrew.splitup.check.domain.Check

data class CheckDetailScreenState(
    val check: Check? = null,
    val isLoading: Boolean = true,
    val isError: Boolean = false
)