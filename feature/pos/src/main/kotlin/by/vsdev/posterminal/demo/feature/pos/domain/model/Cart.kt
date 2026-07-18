package by.vsdev.posterminal.demo.feature.pos.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/** A single line in the cart. Quantity is always >= 1 while the line exists. */
@Parcelize
data class CartLine(val productId: String, val name: String, val priceCents: Long, val quantity: Int) : Parcelable {
    val lineTotalCents: Long get() = priceCents * quantity
}
