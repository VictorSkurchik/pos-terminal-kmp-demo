package by.vsdev.posterminal.demo.core.data.repo

import by.vsdev.posterminal.demo.core.data.local.CartDao
import by.vsdev.posterminal.demo.core.data.local.CartItemEntity
import by.vsdev.posterminal.demo.core.data.mapper.toDomain
import by.vsdev.posterminal.demo.domain.model.CartLine
import by.vsdev.posterminal.demo.domain.model.Product
import by.vsdev.posterminal.demo.domain.repository.CartRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Cart persisted in the local Room DB (survives restarts; cleared on WIPE). */
class CartRepositoryImpl(private val dao: CartDao) : CartRepository {

    override val lines: Flow<List<CartLine>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun add(product: Product) {
        val existing = dao.find(product.id)
        val updated = existing?.copy(quantity = existing.quantity + 1)
            ?: CartItemEntity(product.id, product.name, product.priceCents, 1)
        dao.upsert(updated)
    }

    override suspend fun decrement(productId: String) {
        val existing = dao.find(productId) ?: return
        if (existing.quantity <= 1) {
            dao.delete(productId)
        } else {
            dao.upsert(existing.copy(quantity = existing.quantity - 1))
        }
    }

    override suspend fun remove(productId: String) = dao.delete(productId)

    override suspend fun clear() = dao.clear()
}
