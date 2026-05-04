package com.covildev.splitup.common.utils

import java.math.BigDecimal
import java.util.Locale

object CurrencyUtils {

    /**
     * Converte uma string de entrada (Reais) para centavos (Long).
     * Aceita ponto ou vÃ­rgula como separador decimal.
     * Ex: "12"    -> 1200
     * Ex: "12.50" -> 1250
     * Ex: "0.50"  -> 50
     */
    fun parseToCents(input: String): Long {
        if (input.isBlank()) return 0L
        try {
            // Normaliza: troca vÃ­rgula por ponto para o BigDecimal aceitar
            var normalized = input.replace(",", ".")

            // Remove qualquer caractere que nÃ£o seja nÃºmero ou ponto
            normalized = normalized.replace(Regex("[^0-9.]"), "")

            if (normalized.isBlank()) return 0L

            // PrevenÃ§Ã£o bÃ¡sica contra mÃºltiplos pontos (ex: "12.50.5")
            if (normalized.count { it == '.' } > 1) return 0L

            val amount = BigDecimal(normalized)
            // Multiplica por 100 para virar centavos
            return amount.multiply(BigDecimal(100)).toLong()
        } catch (e: Exception) {
            return 0L
        }
    }

    /**
     * Formata centavos para String decimal (para exibiÃ§Ã£o em campos de texto).
     * Ex: 1200 -> "12.00"
     * Ex: 50   -> "0.50"
     */
    fun formatFromCents(cents: Long): String {
        return try {
            val amount = BigDecimal(cents).divide(BigDecimal(100))
            // ForÃ§a Locale.US para usar ponto, facilitando a re-leitura depois
            String.format(Locale.US, "%.2f", amount)
        } catch (e: Exception) {
            "0.00"
        }
    }
}
