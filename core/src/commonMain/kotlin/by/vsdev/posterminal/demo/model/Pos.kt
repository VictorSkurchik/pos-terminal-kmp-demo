package by.vsdev.posterminal.demo.model

import kotlinx.serialization.Serializable

/** A catalog item on the POS terminal. Price is in minor units (cents) to avoid floats. */
@Serializable
data class Product(val id: String, val name: String, val priceCents: Long, val imageUrl: String? = null)

/** A line item in the cart/order. */
@Serializable
data class OrderItem(val productId: String, val name: String, val priceCents: Long, val quantity: Int) {
    val lineTotalCents: Long get() = priceCents * quantity
}

@Serializable
enum class OrderStatus { CART, PAID, CANCELLED }

/** An order (cart). `total` is computed on the client but stored for history. */
@Serializable
data class Order(
    val id: String,
    val items: List<OrderItem>,
    val totalCents: Long,
    val status: OrderStatus,
    val createdAt: Long,
)
