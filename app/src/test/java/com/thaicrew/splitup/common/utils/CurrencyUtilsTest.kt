package com.thaicrew.splitup.common.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class CurrencyUtilsTest {

    @Test
    fun formatFromCents_and_parseToCents_roundTrip() {
        val cents = 1250L
        val formatted = CurrencyUtils.formatFromCents(cents)
        val parsed = CurrencyUtils.parseToCents(formatted)
        assertEquals(cents, parsed)
    }

    @Test
    fun parseToCents_acceptsIntegersAsReais() {
        assertEquals(1200L, CurrencyUtils.parseToCents("12"))
    }

    @Test
    fun parseToCents_acceptsDecimals() {
        assertEquals(1250L, CurrencyUtils.parseToCents("12.50"))
        assertEquals(1250L, CurrencyUtils.parseToCents("12,50"))
    }
}

