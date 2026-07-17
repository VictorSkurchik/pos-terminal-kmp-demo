package by.vsdev.posterminal.demo.feature.pos.domain.usecase

import by.vsdev.posterminal.demo.domain.policy.DevicePolicy
import by.vsdev.posterminal.demo.feature.pos.domain.model.CartLine
import by.vsdev.posterminal.demo.feature.pos.domain.model.Product
import by.vsdev.posterminal.demo.feature.pos.domain.repository.CartRepository
import by.vsdev.posterminal.demo.feature.pos.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

/** Streams the current cart lines. */
class ObserveCartUseCase(private val cart: CartRepository) {
    operator fun invoke(): Flow<List<CartLine>> = cart.lines
}

/** Streams whether the admin has restricted payment (the "Pay" button). */
class ObservePaymentRestrictedUseCase(private val policy: DevicePolicy) {
    operator fun invoke(): Flow<Boolean> = policy.restrictPayment
}

/** The (static) POS catalog. */
class GetProductsUseCase(private val products: ProductRepository) {
    operator fun invoke(): List<Product> = products.products()
}

/** Adds one unit of a product to the cart. */
class AddToCartUseCase(private val cart: CartRepository) {
    suspend operator fun invoke(product: Product) = cart.add(product)
}

/** Adds one unit of the product with [productId], if it exists in the catalog. */
class IncrementCartItemUseCase(private val cart: CartRepository, private val products: ProductRepository) {
    suspend operator fun invoke(productId: String) {
        products.products().firstOrNull { it.id == productId }?.let { cart.add(it) }
    }
}

/** Removes one unit of a product (deleting the line when it reaches zero). */
class DecrementCartItemUseCase(private val cart: CartRepository) {
    suspend operator fun invoke(productId: String) = cart.decrement(productId)
}

/** Outcome of a checkout attempt. */
sealed interface CheckoutResult {
    data class Paid(val amountCents: Long) : CheckoutResult

    data object EmptyCart : CheckoutResult
}

/** Payment stub: totals the cart, clears it, and reports the paid amount. */
class CheckoutUseCase(private val cart: CartRepository) {
    suspend operator fun invoke(): CheckoutResult {
        val total = cart.lines.first().sumOf { it.lineTotalCents }
        if (total <= 0) return CheckoutResult.EmptyCart
        cart.clear()
        return CheckoutResult.Paid(total)
    }
}
