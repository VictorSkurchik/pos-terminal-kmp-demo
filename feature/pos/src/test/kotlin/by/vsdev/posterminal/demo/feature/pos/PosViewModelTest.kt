package by.vsdev.posterminal.demo.feature.pos

import app.cash.turbine.test
import by.vsdev.posterminal.demo.domain.model.CartLine
import by.vsdev.posterminal.demo.domain.model.Product
import by.vsdev.posterminal.demo.domain.policy.DevicePolicy
import by.vsdev.posterminal.demo.domain.repository.CartRepository
import by.vsdev.posterminal.demo.domain.repository.ProductRepository
import by.vsdev.posterminal.demo.domain.usecase.pos.AddToCartUseCase
import by.vsdev.posterminal.demo.domain.usecase.pos.CheckoutUseCase
import by.vsdev.posterminal.demo.domain.usecase.pos.DecrementCartItemUseCase
import by.vsdev.posterminal.demo.domain.usecase.pos.GetProductsUseCase
import by.vsdev.posterminal.demo.domain.usecase.pos.IncrementCartItemUseCase
import by.vsdev.posterminal.demo.domain.usecase.pos.ObserveCartUseCase
import by.vsdev.posterminal.demo.domain.usecase.pos.ObservePaymentRestrictedUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class PosViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val cart = FakeCart()
    private val catalog = listOf(Product("sku-latte", "Latte", 450))
    private val products = FakeProducts(catalog)
    private val policy = FakeDevicePolicy()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel() = PosViewModel(
        GetProductsUseCase(products),
        ObserveCartUseCase(cart),
        ObservePaymentRestrictedUseCase(policy),
        AddToCartUseCase(cart),
        IncrementCartItemUseCase(cart, products),
        DecrementCartItemUseCase(cart),
        CheckoutUseCase(cart),
    )

    @Test
    fun `initial state exposes the catalog`() {
        assertEquals(catalog, viewModel().state.value.products)
    }

    @Test
    fun `AddToCart intent updates the cart total`() = runTest(dispatcher) {
        val vm = viewModel()
        vm.state.test {
            awaitItem() // initial (empty cart)
            vm.onIntent(PosIntent.AddToCart(catalog.first()))
            advanceUntilIdle()
            val state = awaitItem()
            assertEquals(1, state.cart.size)
            assertEquals(450, state.totalCents)
        }
    }

    @Test
    fun `Checkout intent emits PaymentCompleted with the cart total`() = runTest(dispatcher) {
        val vm = viewModel()
        cart.setLines(listOf(CartLine("sku-latte", "Latte", 450, 2)))
        vm.sideEffect.test {
            vm.onIntent(PosIntent.Checkout)
            advanceUntilIdle()
            assertEquals(PosSideEffect.PaymentCompleted(900), awaitItem())
        }
    }

    private class FakeCart(initial: List<CartLine> = emptyList()) : CartRepository {
        private val state = MutableStateFlow(initial)
        override val lines: Flow<List<CartLine>> = state
        fun setLines(lines: List<CartLine>) { state.value = lines }
        override suspend fun add(product: Product) {
            state.value = state.value + CartLine(product.id, product.name, product.priceCents, 1)
        }
        override suspend fun decrement(productId: String) {
            state.value = state.value.filterNot { it.productId == productId }
        }
        override suspend fun remove(productId: String) = decrement(productId)
        override suspend fun clear() { state.value = emptyList() }
    }

    private class FakeProducts(private val items: List<Product>) : ProductRepository {
        override fun products(): List<Product> = items
    }

    private class FakeDevicePolicy : DevicePolicy {
        override val restrictPayment: Flow<Boolean> = MutableStateFlow(false)
        override val kioskActive: Flow<Boolean> = MutableStateFlow(false)
        override suspend fun setRestrictPayment(value: Boolean) = Unit
        override suspend fun setKioskActive(value: Boolean) = Unit
        override suspend fun reset() = Unit
    }
}
