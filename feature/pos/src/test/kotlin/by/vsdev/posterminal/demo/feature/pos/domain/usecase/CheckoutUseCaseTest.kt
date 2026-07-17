package by.vsdev.posterminal.demo.feature.pos.domain.usecase

import by.vsdev.posterminal.demo.feature.pos.FakeCartRepository
import by.vsdev.posterminal.demo.feature.pos.domain.model.CartLine
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class CheckoutUseCaseTest {

    @Test
    fun `totals the cart, clears it, and reports paid amount`() = runTest {
        val cart = FakeCartRepository(
            listOf(
                CartLine("a", "Espresso", 300, 2),
                CartLine("b", "Latte", 450, 1),
            ),
        )
        val result = CheckoutUseCase(cart).invoke()

        val paid = assertIs<CheckoutResult.Paid>(result)
        assertEquals(1050, paid.amountCents)
        assertTrue(cart.cleared)
    }

    @Test
    fun `empty cart does not charge or clear`() = runTest {
        val cart = FakeCartRepository(emptyList())
        val result = CheckoutUseCase(cart).invoke()

        assertEquals(CheckoutResult.EmptyCart, result)
        assertFalse(cart.cleared)
    }
}
