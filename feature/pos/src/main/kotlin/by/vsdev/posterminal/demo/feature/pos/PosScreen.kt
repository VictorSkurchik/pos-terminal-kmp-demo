package by.vsdev.posterminal.demo.feature.pos

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import by.vsdev.posterminal.demo.core.ui.components.CartPanel
import by.vsdev.posterminal.demo.core.ui.components.MenuGrid
import by.vsdev.posterminal.demo.core.ui.components.PosTopBar
import org.koin.androidx.compose.koinViewModel

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
        Column(Modifier.padding(padding).fillMaxSize()) {
            MenuGrid(
                products = state.products,
                onAdd = viewModel::add,
                modifier = Modifier.weight(1f),
            )
            CartPanel(
                items = state.cart,
                totalCents = state.totalCents,
                payEnabled = !state.payBlocked && state.totalCents > 0,
                payLabel = if (state.payBlocked) "Payment restricted by admin" else "Pay",
                onIncrement = viewModel::increment,
                onDecrement = viewModel::decrement,
                onPay = viewModel::checkout,
                modifier = Modifier.padding(12.dp),
            )
        }
    }
}
