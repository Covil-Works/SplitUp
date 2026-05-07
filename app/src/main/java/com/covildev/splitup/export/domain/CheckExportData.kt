package com.covildev.splitup.export.domain

import com.covildev.splitup.check.domain.Check
import com.covildev.splitup.check.ui.FriendOwedItem

data class FriendExportSummary(
    val friendName: String,
    val items: List<FriendOwedItem>,
    val totalOwed: Long
)

data class CheckExportData(
    val check: Check,
    val itemsSummary: List<ItemExportSummary>,
    val friendsSummary: List<FriendExportSummary>,
    val itemsTotal: Long,      // Soma pura dos itens
    val serviceFee: Long,      // 10%
    val grandTotal: Long       // Soma + 10%
)

data class ItemExportSummary(
    val name: String,
    val unitValue: Long,
    val quantity: Int,
    val totalValue: Long
)
