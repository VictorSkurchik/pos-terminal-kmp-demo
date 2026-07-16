package by.vsdev.posterminal.demo.core.data.mapper

import by.vsdev.posterminal.demo.core.data.local.CartItemEntity
import by.vsdev.posterminal.demo.domain.model.CartLine

fun CartItemEntity.toDomain(): CartLine = CartLine(
    productId = productId,
    name = name,
    priceCents = priceCents,
    quantity = quantity,
)
