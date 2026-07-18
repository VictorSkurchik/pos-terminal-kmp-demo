package by.vsdev.posterminal.demo.feature.pos.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import by.vsdev.posterminal.demo.core.ui.mvi.MviViewModel
import by.vsdev.posterminal.demo.feature.pos.domain.usecase.AddToCartUseCase
import by.vsdev.posterminal.demo.feature.pos.domain.usecase.CheckoutResult
import by.vsdev.posterminal.demo.feature.pos.domain.usecase.CheckoutUseCase
import by.vsdev.posterminal.demo.feature.pos.domain.usecase.DecrementCartItemUseCase
import by.vsdev.posterminal.demo.feature.pos.domain.usecase.GetProductsUseCase
import by.vsdev.posterminal.demo.feature.pos.domain.usecase.IncrementCartItemUseCase
import by.vsdev.posterminal.demo.feature.pos.domain.usecase.ObserveCartUseCase
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class PosViewModel(
    getProducts: GetProductsUseCase,
    observeCart: ObserveCartUseCase,
    private val addToCart: AddToCartUseCase,
    private val incrementItem: IncrementCartItemUseCase,
    private val decrementItem: DecrementCartItemUseCase,
    private val checkout: CheckoutUseCase,
    savedStateHandle: SavedStateHandle,
) : MviViewModel<PosUiState, PosIntent, PosSideEffect>(PosUiState(products = getProducts()), savedStateHandle) {

    init {
        observeCart()
            .onEach { cart ->
                setState { copy(cart = cart, totalCents = cart.sumOf { it.lineTotalCents }) }
            }
            .launchIn(viewModelScope)
    }

    override fun onIntent(intent: PosIntent) {
        when (intent) {
            is PosIntent.AddToCart -> viewModelScope.launch { addToCart(intent.product) }
            is PosIntent.Increment -> viewModelScope.launch { incrementItem(intent.productId) }
            is PosIntent.Decrement -> viewModelScope.launch { decrementItem(intent.productId) }
            PosIntent.Checkout -> viewModelScope.launch {
                when (val result = checkout()) {
                    is CheckoutResult.Paid -> postSideEffect(PosSideEffect.PaymentCompleted(result.amountCents))
                    CheckoutResult.EmptyCart -> Unit
                }
            }
        }
    }
}
