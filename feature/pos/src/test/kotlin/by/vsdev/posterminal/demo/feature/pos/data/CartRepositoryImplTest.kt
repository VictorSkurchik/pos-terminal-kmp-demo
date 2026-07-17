package by.vsdev.posterminal.demo.core.data.repo

import app.cash.turbine.test
import by.vsdev.posterminal.demo.core.data.local.CartDao
import by.vsdev.posterminal.demo.core.data.local.CartItemEntity
import by.vsdev.posterminal.demo.domain.model.Product
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class CartRepositoryImplTest {

    private val dao = FakeCartDao()
    private val repo = CartRepositoryImpl(dao)

    @Test
    fun `adding the same product twice increments its quantity`() = runTest {
        val latte = Product("sku-latte", "Latte", 450)
        repo.add(latte)
        repo.add(latte)

        repo.lines.test {
            val lines = awaitItem()
            assertEquals(1, lines.size)
            assertEquals(2, lines.first().quantity)
            assertEquals(900, lines.first().lineTotalCents)
        }
    }

    @Test
    fun `decrementing the last unit removes the line`() = runTest {
        val latte = Product("sku-latte", "Latte", 450)
        repo.add(latte)
        repo.decrement("sku-latte")

        repo.lines.test {
            assertEquals(0, awaitItem().size)
        }
    }

    private class FakeCartDao : CartDao {
        private val items = linkedMapOf<String, CartItemEntity>()
        private val flow = MutableStateFlow<List<CartItemEntity>>(emptyList())

        override fun observeAll(): Flow<List<CartItemEntity>> = flow
        override suspend fun find(productId: String): CartItemEntity? = items[productId]
        override suspend fun upsert(item: CartItemEntity) {
            items[item.productId] = item
            publish()
        }
        override suspend fun delete(productId: String) {
            items.remove(productId)
            publish()
        }
        override suspend fun clear() {
            items.clear()
            publish()
        }

        private fun publish() {
            flow.value = items.values.sortedBy { it.name }
        }
    }
}
