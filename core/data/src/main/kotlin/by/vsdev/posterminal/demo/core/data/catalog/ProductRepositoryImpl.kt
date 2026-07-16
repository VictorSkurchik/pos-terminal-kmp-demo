package by.vsdev.posterminal.demo.core.data.catalog

import by.vsdev.posterminal.demo.domain.model.Product
import by.vsdev.posterminal.demo.domain.repository.ProductRepository

/** Mock POS catalog (MVP — no server endpoint). Behind [ProductRepository] so it can be faked. */
class ProductRepositoryImpl : ProductRepository {
    override fun products(): List<Product> = CATALOG

    private companion object {
        val CATALOG: List<Product> = listOf(
            Product("sku-espresso", "Espresso", 300),
            Product("sku-latte", "Latte", 450),
            Product("sku-cappuccino", "Cappuccino", 420),
            Product("sku-croissant", "Croissant", 350),
            Product("sku-muffin", "Blueberry Muffin", 380),
            Product("sku-water", "Sparkling Water", 200),
            Product("sku-sandwich", "Club Sandwich", 750),
            Product("sku-cookie", "Chocolate Cookie", 250),
        )
    }
}
