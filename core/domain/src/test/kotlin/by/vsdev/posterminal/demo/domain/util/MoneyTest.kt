package by.vsdev.posterminal.demo.domain.util

import kotlin.test.Test
import kotlin.test.assertEquals

class MoneyTest {

    @Test
    fun `formats whole and fractional dollars`() {
        assertEquals("$12.34", formatCents(1234))
        assertEquals("$0.05", formatCents(5))
        assertEquals("$0.00", formatCents(0))
    }

    @Test
    fun `pads single-digit cents`() {
        assertEquals("$1.09", formatCents(109))
    }

    @Test
    fun `handles negative amounts`() {
        assertEquals("-$3.50", formatCents(-350))
    }

    @Test
    fun `handles large amounts`() {
        assertEquals("$12345.67", formatCents(1_234_567))
    }
}
