package by.vsdev.posterminal.demo.feature.pos.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/** A catalog item on the POS terminal. Price is in minor units (cents) to avoid floats. */
@Parcelize
data class Product(val id: String, val name: String, val priceCents: Long, val imageUrl: String? = null) : Parcelable
