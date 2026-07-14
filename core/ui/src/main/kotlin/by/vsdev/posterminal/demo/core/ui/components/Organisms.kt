package by.vsdev.posterminal.demo.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import by.vsdev.posterminal.demo.model.OrderItem
import by.vsdev.posterminal.demo.model.Product

// ---------- Organisms ----------

@Composable
fun MenuGrid(
    products: List<Product>,
    onAdd: (Product) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(16.dp),
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier,
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(products, key = { it.id }) { product ->
            ProductCard(product = product, onAdd = { onAdd(product) })
        }
    }
}

@Composable
fun CartPanel(
    items: List<OrderItem>,
    totalCents: Long,
    payEnabled: Boolean,
    payLabel: String,
    onIncrement: (String) -> Unit,
    onDecrement: (String) -> Unit,
    onPay: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = MaterialTheme.shapes.large,
        tonalElevation = 2.dp,
    ) {
        Column(Modifier.padding(16.dp)) {
            SectionTitle("Order")
            if (items.isEmpty()) {
                Text(
                    "Cart is empty",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                )
            } else {
                items.forEach { item ->
                    CartLine(
                        item = item,
                        onDecrement = { onDecrement(item.productId) },
                        onIncrement = { onIncrement(item.productId) },
                    )
                }
            }
            HorizontalDivider(Modifier.padding(vertical = 10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Total", style = MaterialTheme.typography.titleMedium)
                PriceText(totalCents, style = MaterialTheme.typography.headlineSmall)
            }
            AppButton(
                text = payLabel,
                onClick = onPay,
                enabled = payEnabled,
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            )
        }
    }
}
