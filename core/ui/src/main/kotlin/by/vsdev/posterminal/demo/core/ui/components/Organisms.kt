package by.vsdev.posterminal.demo.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import by.vsdev.posterminal.demo.core.ui.R
import by.vsdev.posterminal.demo.domain.model.CartLine
import by.vsdev.posterminal.demo.domain.model.Product

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

/**
 * Fixed-height cart overlaid on the menu: an elevated surface with rounded top corners.
 * The order list scrolls within the panel (with bottom padding so the last row clears the divider).
 */
@Composable
fun CartPanel(
    items: List<CartLine>,
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
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        shadowElevation = 8.dp,
        tonalElevation = 2.dp,
    ) {
        Column(Modifier.fillMaxHeight().padding(horizontal = 16.dp, vertical = 12.dp)) {
            SectionTitle(stringResource(R.string.ui_cart))
            Box(Modifier.weight(1f).fillMaxWidth()) {
                if (items.isEmpty()) {
                    Text(
                        stringResource(R.string.ui_cart_empty),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                } else {
                    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                        items.forEach { item ->
                            CartLineRow(
                                item = item,
                                onDecrement = { onDecrement(item.productId) },
                                onIncrement = { onIncrement(item.productId) },
                            )
                        }
                        // Breathing room so the last row isn't clipped by the divider above Total.
                        Spacer(Modifier.height(12.dp))
                    }
                }
            }
            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(stringResource(R.string.ui_total), style = MaterialTheme.typography.titleMedium)
                PriceText(totalCents, style = MaterialTheme.typography.headlineSmall)
            }
            AppButton(
                text = payLabel,
                onClick = onPay,
                enabled = payEnabled,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            )
        }
    }
}
