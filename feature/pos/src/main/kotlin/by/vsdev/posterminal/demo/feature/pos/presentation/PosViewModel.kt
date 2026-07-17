package by.vsdev.posterminal.demo.feature.pos.presentation

import androidx.lifecycle.viewModelScope
import by.vsdev.posterminal.demo.core.ui.mvi.MviViewModel
import by.vsdev.posterminal.demo.feature.pos.domain.usecase.AddToCartUseCase
import by.vsdev.posterminal.demo.feature.pos.domain.usecase.CheckoutResult
import by.vsdev.posterminal.demo.feature.pos.domain.usecase.CheckoutUseCase
import by.vsdev.posterminal.demo.feature.pos.domain.usecase.DecrementCartItemUseCase
import by.vsdev.posterminal.demo.feature.pos.domain.usecase.GetProductsUseCase
import by.vsdev.posterminal.demo.feature.pos.domain.usecase.IncrementCartItemUseCase
import by.vsdev.posterminal.demo.feature.pos.domain.usecase.ObserveCartUseCase
import by.vsdev.posterminal.demo.feature.pos.domain.usecase.ObservePaymentRestrictedUseCase
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class PosViewModel(
    getProducts: GetProductsUseCase,
    observeCart: ObserveCartUseCase,
    observePaymentRestricted: ObservePaymentRestrictedUseCase,
    private val addToCart: AddToCartUseCase,
    private val incrementItem: IncrementCartItemUseCase,
    private val decrementItem: DecrementCartItemUseCase,
    private val checkout: CheckoutUseCase,
) : MviViewModel<PosUiState, PosIntent, PosSideEffect>(PosUiState(products = getProducts())) {

    init {
        combine(observeCart(), observePaymentRestricted()) { cart, restricted -> cart to restricted }
            .onEach { (cart, restricted) ->
                setState {
                    copy(
                        cart = cart,
                        totalCents = cart.sumOf { it.lineTotalCents },
                        payBlocked = restricted,
                    )
                }
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
