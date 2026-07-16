package by.vsdev.posterminal.demo.feature.pos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import by.vsdev.posterminal.demo.domain.model.CartLine
import by.vsdev.posterminal.demo.domain.model.Product
import by.vsdev.posterminal.demo.domain.usecase.pos.AddToCartUseCase
import by.vsdev.posterminal.demo.domain.usecase.pos.CheckoutResult
import by.vsdev.posterminal.demo.domain.usecase.pos.CheckoutUseCase
import by.vsdev.posterminal.demo.domain.usecase.pos.DecrementCartItemUseCase
import by.vsdev.posterminal.demo.domain.usecase.pos.GetProductsUseCase
import by.vsdev.posterminal.demo.domain.usecase.pos.IncrementCartItemUseCase
import by.vsdev.posterminal.demo.domain.usecase.pos.ObserveCartUseCase
import by.vsdev.posterminal.demo.domain.usecase.pos.ObservePaymentRestrictedUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PosUiState(
    val products: List<Product> = emptyList(),
    val cart: List<CartLine> = emptyList(),
    val totalCents: Long = 0,
    val payBlocked: Boolean = false,
)

/** One-shot POS events; the screen maps them to localized snackbar text. */
sealed interface PosEvent {
    data class PaymentCompleted(val amountCents: Long) : PosEvent
}

class PosViewModel(
    getProducts: GetProductsUseCase,
    observeCart: ObserveCartUseCase,
    observePaymentRestricted: ObservePaymentRestrictedUseCase,
    private val addToCart: AddToCartUseCase,
    private val incrementItem: IncrementCartItemUseCase,
    private val decrementItem: DecrementCartItemUseCase,
    private val checkout: CheckoutUseCase,
) : ViewModel() {

    private val products = getProducts()

    val uiState: StateFlow<PosUiState> =
        combine(observeCart(), observePaymentRestricted()) { cart, restricted ->
            PosUiState(
                products = products,
                cart = cart,
                totalCents = cart.sumOf { it.lineTotalCents },
                payBlocked = restricted,
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            PosUiState(products = products),
        )

    private val eventsChannel = Channel<PosEvent>(Channel.BUFFERED)
    val events: Flow<PosEvent> = eventsChannel.receiveAsFlow()

    fun add(product: Product) = viewModelScope.launch { addToCart(product) }

    fun increment(productId: String) = viewModelScope.launch { incrementItem(productId) }

    fun decrement(productId: String) = viewModelScope.launch { decrementItem(productId) }

    fun checkout() = viewModelScope.launch {
        when (val result = checkout.invoke()) {
            is CheckoutResult.Paid -> eventsChannel.send(PosEvent.PaymentCompleted(result.amountCents))
            CheckoutResult.EmptyCart -> Unit
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
