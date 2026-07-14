package by.vsdev.posterminal.demo.feature.pos

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import by.vsdev.posterminal.demo.core.ui.components.CartPanel
import by.vsdev.posterminal.demo.core.ui.components.MenuGrid
import by.vsdev.posterminal.demo.core.ui.components.PosTopBar
import org.koin.androidx.compose.koinViewModel

private val CartHeight = 260.dp

@Composable
fun PosScreen(
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PosViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val receipt by viewModel.receiptMessage.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(receipt) {
        receipt?.let {
            snackbar.showSnackbar(it)
            viewModel.consumeReceipt()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { PosTopBar(title = "Restaurant POS", onSettingsClick = onOpenSettings) },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            // Menu spans topbar → bottom, scrolling behind the cart; bottom padding = cart height
            // so the last items can be scrolled fully into view above the cart.
            MenuGrid(
                products = state.products,
                onAdd = viewModel::add,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = CartHeight + 24.dp),
            )
            CartPanel(
                items = state.cart,
                totalCents = state.totalCents,
                payEnabled = !state.payBlocked && state.totalCents > 0,
                payLabel = if (state.payBlocked) "Payment restricted by admin" else "Pay",
                onIncrement = viewModel::increment,
                onDecrement = viewModel::decrement,
                onPay = viewModel::checkout,
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(CartHeight),
            )
        }
    }
}
