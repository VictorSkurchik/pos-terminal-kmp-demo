package by.vsdev.posterminal.demo.feature.pos.presentation

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import by.vsdev.posterminal.demo.core.ui.theme.PosTheme
import by.vsdev.posterminal.demo.domain.util.formatCents
import by.vsdev.posterminal.demo.feature.pos.R
import by.vsdev.posterminal.demo.feature.pos.domain.model.CartLine
import by.vsdev.posterminal.demo.feature.pos.domain.model.Product
import by.vsdev.posterminal.demo.feature.pos.presentation.components.CartPanel
import by.vsdev.posterminal.demo.feature.pos.presentation.components.MenuGrid
import by.vsdev.posterminal.demo.feature.pos.presentation.components.PosTopBar
import org.koin.androidx.compose.koinViewModel

private val CartHeight = 260.dp

@Composable
fun PosScreen(onOpenSettings: () -> Unit, modifier: Modifier = Modifier, viewModel: PosViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is PosSideEffect.PaymentCompleted ->
                    snackbar.showSnackbar(context.getString(R.string.pos_paid, formatCents(effect.amountCents)))
            }
        }
    }

    PosContent(
        state = state,
        snackbar = snackbar,
        onOpenSettings = onOpenSettings,
        onIntent = viewModel::onIntent,
        modifier = modifier,
    )
}

@Composable
private fun PosContent(
    state: PosUiState,
    snackbar: SnackbarHostState,
    onOpenSettings: () -> Unit,
    onIntent: (PosIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { PosTopBar(title = stringResource(R.string.pos_title), onSettingsClick = onOpenSettings) },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Box(
            Modifier
                .padding(padding)
                .fillMaxSize(),
        ) {
            // Menu spans topbar → bottom, scrolling behind the cart; bottom padding = cart height
            // so the last items can be scrolled fully into view above the cart.
            MenuGrid(
                products = state.products,
                onAdd = { onIntent(PosIntent.AddToCart(it)) },
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = CartHeight + 24.dp),
            )
            CartPanel(
                items = state.cart,
                totalCents = state.totalCents,
                payEnabled = !state.payBlocked && state.totalCents > 0,
                payLabel = stringResource(
                    if (state.payBlocked) R.string.pos_payment_restricted else R.string.pos_pay,
                ),
                onIncrement = { onIntent(PosIntent.Increment(it)) },
                onDecrement = { onIntent(PosIntent.Decrement(it)) },
                onPay = { onIntent(PosIntent.Checkout) },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(CartHeight),
            )
        }
    }
}

@Preview
@Composable
private fun PosPreview() {
    val products = listOf(
        Product("sku-espresso", "Espresso", 300),
        Product("sku-latte", "Latte", 450),
    )
    PosTheme {
        PosContent(
            state = PosUiState(
                products = products,
                cart = listOf(CartLine("sku-latte", "Latte", 450, 2)),
                totalCents = 900,
            ),
            snackbar = remember { SnackbarHostState() },
            onOpenSettings = {},
            onIntent = {},
        )
    }
}
