package com.covildev.splitup.export.domain

import com.covildev.splitup.check.domain.*
import com.covildev.splitup.check.ui.FriendOwedItem
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class PrepareCheckExportDataUseCase @Inject constructor(
    private val getCheckByIdUseCase: GetCheckByIdUseCase,
    private val getCheckItemsWithSharersUseCase: GetCheckItemsWithSharersUseCase,
    private val getParticipantsUseCase: GetParticipantsUseCase
) {
    suspend operator fun invoke(checkId: Int): CheckExportData {
        val check = getCheckByIdUseCase(checkId) ?: throw Exception("Comanda nÃ£o encontrada")
        val itemsWithSharers = getCheckItemsWithSharersUseCase(checkId).first()
        val participants = getParticipantsUseCase(checkId).first()

        // 1. Resumo por Itens
        val itemsSummary = itemsWithSharers.map { entry ->
            ItemExportSummary(
                name = entry.item.name,
                unitValue = entry.item.valueInCents,
                quantity = entry.item.quantity,
                totalValue = entry.item.valueInCents * entry.item.quantity
            )
        }

        // 2. Resumo por Amigos
        val friendsSummary = participants.map { friend ->
            val owedItems = itemsWithSharers
                .filter { it.sharersIds.contains(friend.id) }
                .map { entry ->
                    FriendOwedItem(
                        itemId = entry.item.id,
                        itemName = entry.item.name,
                        quantity = entry.item.quantity,
                        unitValueInCents = entry.item.valueInCents,
                        amountInCents = (entry.item.valueInCents * entry.item.quantity) / entry.sharersIds.size
                    )
                }
            FriendExportSummary(
                friendName = friend.name,
                items = owedItems,
                totalOwed = owedItems.sumOf { it.amountInCents }
            )

        }

        // 3. Totais Globais
        val itemsTotal = itemsSummary.sumOf { it.totalValue }
        val serviceFee = (itemsTotal * 0.10).toLong() // 10% simples
        val grandTotal = itemsTotal + serviceFee

        return CheckExportData(
            check = check,
            itemsSummary = itemsSummary,
            friendsSummary = friendsSummary,
            itemsTotal = itemsTotal,
            serviceFee = serviceFee,
            grandTotal = grandTotal
        )
    }
}
