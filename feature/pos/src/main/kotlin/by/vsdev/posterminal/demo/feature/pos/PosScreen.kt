package by.vsdev.posterminal.demo.feature.pos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import by.vsdev.posterminal.demo.model.OrderItem
import by.vsdev.posterminal.demo.model.Product
import by.vsdev.posterminal.demo.util.formatCents
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosScreen(
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
        topBar = { TopAppBar(title = { Text("POS Terminal") }) },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            Text(
                "Catalog",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp),
            )
            LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                items(state.products, key = { it.id }) { product ->
                    ProductRow(product) { viewModel.add(product) }
                }
            }
            CartSummary(
                cart = state.cart,
                totalCents = state.totalCents,
                payBlocked = state.payBlocked,
                onIncrement = viewModel::increment,
                onDecrement = viewModel::decrement,
                onPay = viewModel::checkout,
            )
        }
    }
}

@Composable
private fun ProductRow(product: Product, onAdd: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(product.name, style = MaterialTheme.typography.bodyLarge)
            Text(formatCents(product.priceCents), style = MaterialTheme.typography.bodyMedium)
        }
        OutlinedButton(onClick = onAdd) { Text("Add") }
    }
}

@Composable
private fun CartSummary(
    cart: List<OrderItem>,
    totalCents: Long,
    payBlocked: Boolean,
    onIncrement: (String) -> Unit,
    onDecrement: (String) -> Unit,
    onPay: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text("Cart", style = MaterialTheme.typography.titleMedium)
            if (cart.isEmpty()) {
                Text("Empty", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp))
            } else {
                cart.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(item.name, modifier = Modifier.weight(1f))
                        IconButton(onClick = { onDecrement(item.productId) }) { Text("−") }
                        Text("${item.quantity}")
                        IconButton(onClick = { onIncrement(item.productId) }) { Text("+") }
                        Text(
                            formatCents(item.lineTotalCents),
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                }
            }
            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Total", style = MaterialTheme.typography.titleMedium)
                Text(
                    formatCents(totalCents),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                )
            }
            Button(
                onClick = onPay,
                enabled = !payBlocked && totalCents > 0,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            ) {
                Text(if (payBlocked) "Payment restricted by admin" else "Pay")
            }
        }
    }
}
