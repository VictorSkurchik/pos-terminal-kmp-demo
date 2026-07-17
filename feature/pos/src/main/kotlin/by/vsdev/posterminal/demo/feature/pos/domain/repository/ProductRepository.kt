package by.vsdev.posterminal.demo.feature.pos.domain.repository

import by.vsdev.posterminal.demo.feature.pos.domain.model.Product

/** Source of the POS catalog. Behind an interface so tests can supply a fixed list. */
interface ProductRepository {
    fun products(): List<Product>
}
