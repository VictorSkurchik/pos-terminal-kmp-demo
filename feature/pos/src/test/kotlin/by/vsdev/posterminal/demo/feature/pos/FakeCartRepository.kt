package by.vsdev.posterminal.demo.feature.pos

import by.vsdev.posterminal.demo.feature.pos.domain.model.CartLine
import by.vsdev.posterminal.demo.feature.pos.domain.model.Product
import by.vsdev.posterminal.demo.feature.pos.domain.repository.CartRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** In-memory [CartRepository] for pos use-case tests. */
class FakeCartRepository(initial: List<CartLine> = emptyList()) : CartRepository {
    private val state = MutableStateFlow(initial)
    override val lines: Flow<List<CartLine>> = state
    var cleared = false
        private set

    fun setLines(lines: List<CartLine>) {
        state.value = lines
    }

    override suspend fun add(product: Product) {
        state.value = state.value + CartLine(product.id, product.name, product.priceCents, 1)
    }

    override suspend fun decrement(productId: String) {
        state.value = state.value.filterNot { it.productId == productId }
    }

    override suspend fun remove(productId: String) = decrement(productId)

    override suspend fun clear() {
        cleared = true
        state.value = emptyList()
    }
}
