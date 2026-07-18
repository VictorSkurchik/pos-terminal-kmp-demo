package by.vsdev.posterminal.demo.feature.pos.presentation

import android.os.Parcelable
import by.vsdev.posterminal.demo.core.ui.mvi.UiIntent
import by.vsdev.posterminal.demo.core.ui.mvi.UiSideEffect
import by.vsdev.posterminal.demo.core.ui.mvi.UiState
import by.vsdev.posterminal.demo.feature.pos.domain.model.CartLine
import by.vsdev.posterminal.demo.feature.pos.domain.model.Product
import kotlinx.parcelize.Parcelize

/** MVI contract for the POS screen. */
@Parcelize
data class PosUiState(
    val products: List<Product> = emptyList(),
    val cart: List<CartLine> = emptyList(),
    val totalCents: Long = 0,
) : UiState,
    Parcelable

sealed interface PosIntent : UiIntent {
    data class AddToCart(val product: Product) : PosIntent

    data class Increment(val productId: String) : PosIntent

    data class Decrement(val productId: String) : PosIntent

    data object Checkout : PosIntent
}

sealed interface PosSideEffect : UiSideEffect {
    data class PaymentCompleted(val amountCents: Long) : PosSideEffect
}
