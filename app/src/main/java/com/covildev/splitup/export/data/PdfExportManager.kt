package com.covildev.splitup.export.data

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.covildev.splitup.export.domain.CheckExportData
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class PdfExportManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun generateCheckPdf(data: CheckExportData): File {
        // Aumentei a altura da pÃ¡gina para garantir que caiba tudo
        val pageInfo = PdfDocument.PageInfo.Builder(300, 800, 1).create()
        val pdfDocument = PdfDocument()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paint = Paint()
        var y = 40f

        // --- 1. CABEÃ‡ALHO ---
        paint.textSize = 18f
        paint.isFakeBoldText = true
        canvas.drawText("SplitUp", 20f, y, paint)

        y += 25f
        paint.textSize = 14f
        paint.isFakeBoldText = false
        canvas.drawText(data.check.name, 20f, y, paint)

        y += 20f
        paint.textSize = 10f
        paint.color = 0xFF666666.toInt() // Cinza escuro

        // FormataÃ§Ã£o visual da data (dentro do PDF)
        val dateObj = Date(data.check.creationDate)
        val visualDateFormat = SimpleDateFormat("dd/MM/yyyy 'Ã s' HH:mm", Locale("pt", "BR"))
        canvas.drawText(visualDateFormat.format(dateObj), 20f, y, paint)

        paint.color = 0xFF000000.toInt() // Preto

        // --- 2. ITENS ---
        y += 40f
        paint.textSize = 12f
        paint.isFakeBoldText = true
        canvas.drawText("RESUMO POR ITENS", 20f, y, paint)
        paint.isFakeBoldText = false

        y += 5f
        paint.strokeWidth = 1f
        canvas.drawLine(20f, y, 280f, y, paint)

        data.itemsSummary.forEach { item ->
            y += 20f
            val name = if (item.name.length > 25) item.name.take(22) + "..." else item.name
            canvas.drawText("${item.quantity}x $name", 20f, y, paint)

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
        canvas.drawLine(20f, y, 280f, y, paint)

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
                val truncatedItem = if(itemLine.length > 30) itemLine.take(27)+"..." else itemLine
                canvas.drawText(truncatedItem, 30f, y, paint)

                val valLine = "R$ ${String.format("%.2f", owed.amountInCents / 100.0)}"
                val valWidth = paint.measureText(valLine)
                canvas.drawText(valLine, 280f - valWidth, y, paint)
            }
            paint.textSize = 12f
        }

        // --- 4. TOTAIS FINAIS ---
        y += 50f
        canvas.drawLine(20f, y, 280f, y, paint)

        // Total Itens
        y += 20f
        paint.textSize = 12f
        canvas.drawText("Total Itens:", 20f, y, paint)
        val subText = "R$ ${String.format("%.2f", data.itemsTotal / 100.0)}"
        canvas.drawText(subText, 280f - paint.measureText(subText), y, paint)

        // 10%
        y += 18f
        paint.textSize = 11f
        paint.color = 0xFF555555.toInt()
        canvas.drawText("ServiÃ§o (10%):", 20f, y, paint)
        val servText = "R$ ${String.format("%.2f", data.serviceFee / 100.0)}"
        canvas.drawText(servText, 280f - paint.measureText(servText), y, paint)

        // Total Geral
        y += 25f
        paint.color = 0xFF000000.toInt()
        paint.textSize = 16f
        paint.isFakeBoldText = true
        canvas.drawText("TOTAL GERAL:", 20f, y, paint)
        val grandText = "R$ ${String.format("%.2f", data.grandTotal / 100.0)}"
        canvas.drawText(grandText, 280f - paint.measureText(grandText), y, paint)

        pdfDocument.finishPage(page)

        // --- SALVAR O ARQUIVO ---
        val exportDir = File(context.cacheDir, "export")
        if (!exportDir.exists()) exportDir.mkdirs()

        // 1. Sanitiza o nome (troca espaÃ§o por _ e remove caracteres estranhos)
        val safeCheckName = data.check.name
            .replace(" ", "_")
            .replace(Regex("[^a-zA-Z0-9_\\-]"), "")
            .take(30) // Limita tamanho para nÃ£o ficar gigante

        // 2. Formata a data para arquivo (yyyyMMdd)
        val fileDateFormat = SimpleDateFormat("yyyyMMdd", Locale.US)
        val dateSuffix = fileDateFormat.format(dateObj)

        // 3. Monta o nome final: splitup-Bar_do_Ze-20231027.pdf
        val fileName = "splitup-${safeCheckName}-${dateSuffix}.pdf"

        val file = File(exportDir, fileName)

        // Se o arquivo jÃ¡ existir, deleta para criar o novo
        if (file.exists()) file.delete()

        pdfDocument.writeTo(FileOutputStream(file))
        pdfDocument.close()

        return file
    }
}
