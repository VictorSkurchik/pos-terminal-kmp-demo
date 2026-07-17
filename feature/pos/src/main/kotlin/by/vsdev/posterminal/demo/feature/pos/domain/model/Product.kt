package by.vsdev.posterminal.demo.feature.pos.domain.model

/** A catalog item on the POS terminal. Price is in minor units (cents) to avoid floats. */
data class Product(val id: String, val name: String, val priceCents: Long, val imageUrl: String? = null)
