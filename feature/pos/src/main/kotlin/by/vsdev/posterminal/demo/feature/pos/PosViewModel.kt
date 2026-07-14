package by.vsdev.posterminal.demo.feature.pos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import by.vsdev.posterminal.demo.core.data.catalog.ProductCatalog
import by.vsdev.posterminal.demo.core.data.prefs.SettingsRepository
import by.vsdev.posterminal.demo.core.data.repo.CartRepository
import by.vsdev.posterminal.demo.model.OrderItem
import by.vsdev.posterminal.demo.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PosUiState(
    val products: List<Product> = ProductCatalog.products,
    val cart: List<OrderItem> = emptyList(),
    val totalCents: Long = 0,
    val payBlocked: Boolean = false,
)

class PosViewModel(
    private val cart: CartRepository,
    settings: SettingsRepository,
) : ViewModel() {

    val uiState: StateFlow<PosUiState> =
        combine(cart.items, settings.restrictApp) { items, restricted ->
            PosUiState(
                products = ProductCatalog.products,
                cart = items,
                totalCents = items.sumOf { it.lineTotalCents },
                payBlocked = restricted,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PosUiState())

    /** One-shot "payment" message for the snackbar. */
    val receiptMessage = MutableStateFlow<String?>(null)

    fun add(product: Product) = viewModelScope.launch { cart.add(product) }
    fun increment(productId: String) = viewModelScope.launch {
        uiState.value.products.firstOrNull { it.id == productId }?.let { cart.add(it) }
    }
    fun decrement(productId: String) = viewModelScope.launch { cart.decrement(productId) }
    fun remove(productId: String) = viewModelScope.launch { cart.remove(productId) }

    /** Payment stub: records a "receipt" and clears the cart. */
    fun checkout() = viewModelScope.launch {
        val total = uiState.value.totalCents
        if (total <= 0) return@launch
        cart.clear()
        receiptMessage.value = "Paid ${by.vsdev.posterminal.demo.util.formatCents(total)} (stub)"
    }

    fun consumeReceipt() { receiptMessage.value = null }
}
