package by.vsdev.posterminal.demo.feature.pos.data

import by.vsdev.posterminal.demo.feature.pos.data.local.CartItemEntity
import by.vsdev.posterminal.demo.feature.pos.domain.model.CartLine

fun CartItemEntity.toDomain(): CartLine = CartLine(
    productId = productId,
    name = name,
    priceCents = priceCents,
    quantity = quantity,
)
