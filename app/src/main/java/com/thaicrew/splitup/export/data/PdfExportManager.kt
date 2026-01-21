package com.thaicrew.splitup.export.data

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.thaicrew.splitup.export.domain.CheckExportData
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class PdfExportManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun generateCheckPdf(data: CheckExportData): File {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(300, 600, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paint = Paint()
        var y = 40f

        // Cabeçalho
        paint.textSize = 16f
        paint.isFakeBoldText = true
        canvas.drawText("SplitUp - ${data.check.name}", 20f, y, paint)

        y += 30f
        paint.textSize = 10f
        paint.isFakeBoldText = false
        canvas.drawText("Data: ${data.check.creationDate}", 20f, y, paint)

        // Itens
        y += 40f
        paint.isFakeBoldText = true
        canvas.drawText("RESUMO POR ITENS", 20f, y, paint)
        paint.isFakeBoldText = false

        data.itemsSummary.forEach { item ->
            y += 20f
            canvas.drawText("${item.quantity}x ${item.name}", 20f, y, paint)
            canvas.drawText("R$ ${String.format("%.2f", item.totalValue / 100.0)}", 220f, y, paint)
        }

        // Amigos
        y += 40f
        paint.isFakeBoldText = true
        canvas.drawText("RESUMO POR PESSOA", 20f, y, paint)
        paint.isFakeBoldText = false

        data.friendsSummary.forEach { friend ->
            y += 25f
            paint.isFakeBoldText = true
            canvas.drawText(friend.friendName, 20f, y, paint)
            canvas.drawText("Total: R$ ${String.format("%.2f", friend.totalOwed / 100.0)}", 180f, y, paint)
            paint.isFakeBoldText = false

            friend.items.forEach { owed ->
                y += 15f
                canvas.drawText("- ${owed.itemName}: R$ ${String.format("%.2f", owed.amountInCents / 100.0)}", 30f, y, paint)
            }
        }

        // Total Geral
        y += 50f
        paint.textSize = 14f
        paint.isFakeBoldText = true
        canvas.drawText("TOTAL GERAL: R$ ${String.format("%.2f", data.grandTotal / 100.0)}", 20f, y, paint)

        pdfDocument.finishPage(page)

        // Salvar no Cache
        val exportDir = File(context.cacheDir, "export")
        if (!exportDir.exists()) exportDir.mkdirs()

        val file = File(exportDir, "comanda_${data.check.id}.pdf")
        pdfDocument.writeTo(FileOutputStream(file))
        pdfDocument.close()

        return file
    }
}