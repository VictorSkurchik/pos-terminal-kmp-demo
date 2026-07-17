package by.vsdev.posterminal.demo.feature.pos.domain.model

/** A single line in the cart. Quantity is always >= 1 while the line exists. */
data class CartLine(val productId: String, val name: String, val priceCents: Long, val quantity: Int) {
    val lineTotalCents: Long get() = priceCents * quantity
}
