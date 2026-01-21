package com.thaicrew.splitup.export.data

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.thaicrew.splitup.export.domain.CheckExportData
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat // Importante para a data
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class PdfExportManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun generateCheckPdf(data: CheckExportData): File {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(300, 800, 1).create() // Aumentei a altura para caber tudo
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paint = Paint()
        var y = 40f

        // --- 1. CABEÇALHO FORMATADO ---
        paint.textSize = 18f
        paint.isFakeBoldText = true
        canvas.drawText("SplitUp", 20f, y, paint)

        y += 25f
        paint.textSize = 14f
        paint.isFakeBoldText = false
        canvas.drawText(data.check.name, 20f, y, paint)

        y += 20f
        paint.textSize = 10f
        paint.color = 0xFF666666.toInt() // Cinza escuro para a data

        // Formatação da Data (Assumindo que createdAt é Long em millis)
        val dateFormat = SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale("pt", "BR"))
        val dateString = try {
            dateFormat.format(Date(data.check.creationDate)) // Se for Long
        } catch (e: Exception) {
            data.check.creationDate.toString() // Fallback
        }
        canvas.drawText(dateString, 20f, y, paint)
        paint.color = 0xFF000000.toInt() // Volta para preto

        // --- 2. ITENS ---
        y += 40f
        paint.textSize = 12f
        paint.isFakeBoldText = true
        canvas.drawText("RESUMO POR ITENS", 20f, y, paint)
        paint.isFakeBoldText = false

        // Linha divisória
        y += 5f
        paint.strokeWidth = 1f
        canvas.drawLine(20f, y, 280f, y, paint)

        data.itemsSummary.forEach { item ->
            y += 20f
            // Nome do item (limitado visualmente)
            val name = if (item.name.length > 25) item.name.take(22) + "..." else item.name
            canvas.drawText("${item.quantity}x $name", 20f, y, paint)

            // Valor alinhado à direita
            val valueText = "R$ ${String.format("%.2f", item.totalValue / 100.0)}"
            val textWidth = paint.measureText(valueText)
            canvas.drawText(valueText, 280f - textWidth, y, paint)
        }

        // --- 3. AMIGOS ---
        y += 40f
        paint.textSize = 12f
        paint.isFakeBoldText = true
        canvas.drawText("RESUMO POR PESSOA", 20f, y, paint)
        paint.isFakeBoldText = false

        y += 5f
        canvas.drawLine(20f, y, 280f, y, paint) // Linha divisória

        data.friendsSummary.forEach { friend ->
            y += 25f
            paint.isFakeBoldText = true
            canvas.drawText(friend.friendName, 20f, y, paint)

            val totalFriend = "Total: R$ ${String.format("%.2f", friend.totalOwed / 100.0)}"
            val totalWidth = paint.measureText(totalFriend)
            canvas.drawText(totalFriend, 280f - totalWidth, y, paint)

            paint.isFakeBoldText = false
            paint.textSize = 10f

            friend.items.forEach { owed ->
                y += 15f
                val itemLine = "- ${owed.itemName}"
                canvas.drawText(if(itemLine.length > 30) itemLine.take(27)+"..." else itemLine, 30f, y, paint)

                val valLine = "R$ ${String.format("%.2f", owed.amountInCents / 100.0)}"
                val valWidth = paint.measureText(valLine)
                canvas.drawText(valLine, 280f - valWidth, y, paint)
            }
            paint.textSize = 12f // Restaura tamanho
        }

        // --- 4. TOTAIS FINAIS (COM 10%) ---
        y += 50f
        canvas.drawLine(20f, y, 280f, y, paint) // Linha antes dos totais

        // Subtotal (Total Itens)
        y += 20f
        paint.textSize = 12f
        canvas.drawText("Total Itens:", 20f, y, paint)
        val subText = "R$ ${String.format("%.2f", data.itemsTotal / 100.0)}"
        canvas.drawText(subText, 280f - paint.measureText(subText), y, paint)

        // 10% (Menos destaque)
        y += 18f
        paint.textSize = 11f
        paint.color = 0xFF555555.toInt() // Cinza
        canvas.drawText("Serviço (10%):", 20f, y, paint)
        val servText = "R$ ${String.format("%.2f", data.serviceFee / 100.0)}"
        canvas.drawText(servText, 280f - paint.measureText(servText), y, paint)

        // Total Final (Destaque)
        y += 25f
        paint.color = 0xFF000000.toInt() // Preto
        paint.textSize = 16f
        paint.isFakeBoldText = true
        canvas.drawText("TOTAL GERAL:", 20f, y, paint)
        val grandText = "R$ ${String.format("%.2f", data.grandTotal / 100.0)}"
        canvas.drawText(grandText, 280f - paint.measureText(grandText), y, paint)

        pdfDocument.finishPage(page)

        // Salvar
        val exportDir = File(context.cacheDir, "export")
        if (!exportDir.exists()) exportDir.mkdirs()

        val file = File(exportDir, "comanda_${data.check.id}.pdf")
        pdfDocument.writeTo(FileOutputStream(file))
        pdfDocument.close()

        return file
    }
}