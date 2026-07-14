package by.vsdev.posterminal.demo.core.data.catalog

import by.vsdev.posterminal.demo.model.Product

/** Mock POS product catalog (MVP — no server endpoint). */
object ProductCatalog {
    val products: List<Product> = listOf(
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
