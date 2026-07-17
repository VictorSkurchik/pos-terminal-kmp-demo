package by.vsdev.posterminal.demo.domain.model

/** A single line in the cart. Quantity is always >= 1 while the line exists. */
data class CartLine(
    val productId: String,
    val name: String,
    val priceCents: Long,
    val quantity: Int,
) {
    val lineTotalCents: Long get() = priceCents * quantity
}

/** The cart as a whole, with its total derived from the lines. */
data class Cart(
    val lines: List<CartLine> = emptyList(),
) {
    val totalCents: Long get() = lines.sumOf { it.lineTotalCents }
    val isEmpty: Boolean get() = lines.isEmpty()
}
