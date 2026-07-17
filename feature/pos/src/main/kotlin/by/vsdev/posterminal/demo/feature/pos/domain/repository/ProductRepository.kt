package by.vsdev.posterminal.demo.domain.repository

import by.vsdev.posterminal.demo.domain.model.Product

/** Source of the POS catalog. Behind an interface so tests can supply a fixed list. */
interface ProductRepository {
    fun products(): List<Product>
}
