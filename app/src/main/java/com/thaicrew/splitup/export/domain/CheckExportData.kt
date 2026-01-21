package com.thaicrew.splitup.export.domain

import com.thaicrew.splitup.check.domain.Check
import com.thaicrew.splitup.check.ui.FriendOwedItem

data class FriendExportSummary(
    val friendName: String,
    val items: List<FriendOwedItem>,
    val totalOwed: Long
)

data class CheckExportData(
    val check: Check,
    val itemsSummary: List<ItemExportSummary>,
    val friendsSummary: List<FriendExportSummary>,
    val grandTotal: Long
)

data class ItemExportSummary(
    val name: String,
    val unitValue: Long,
    val quantity: Int,
    val totalValue: Long
)