package by.vsdev.posterminal.demo.domain.repository

import by.vsdev.posterminal.demo.domain.model.CartLine
import by.vsdev.posterminal.demo.domain.model.Product
import kotlinx.coroutines.flow.Flow

/** The persisted cart. */
interface CartRepository {
    val lines: Flow<List<CartLine>>

    suspend fun add(product: Product)

    suspend fun decrement(productId: String)

    suspend fun remove(productId: String)

    suspend fun clear()
}
