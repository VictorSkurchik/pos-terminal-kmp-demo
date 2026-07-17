package by.vsdev.posterminal.demo.feature.pos.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import by.vsdev.posterminal.demo.core.ui.R
import by.vsdev.posterminal.demo.core.ui.components.AppButton
import by.vsdev.posterminal.demo.core.ui.components.AppButtonVariant
import by.vsdev.posterminal.demo.core.ui.components.SectionTitle
import by.vsdev.posterminal.demo.domain.util.formatCents
import by.vsdev.posterminal.demo.feature.pos.domain.model.CartLine
import by.vsdev.posterminal.demo.feature.pos.domain.model.Product
import coil3.compose.AsyncImage

// POS-specific presentation components (moved out of the shared :core:ui design system, which stays
// generic). They compose the generic atoms (AppButton, SectionTitle) with the POS domain models.

@Composable
fun PriceText(
    cents: Long,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.titleMedium,
    color: Color = MaterialTheme.colorScheme.onSurface,
) {
    Text(text = formatCents(cents), modifier = modifier, style = style, fontWeight = FontWeight.Bold, color = color)
}

@Composable
fun QuantityStepper(quantity: Int, onDecrement: () -> Unit, onIncrement: () -> Unit, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        FilledTonalIconButton(onClick = onDecrement, modifier = Modifier.size(32.dp)) { Text("−") }
        Text(
            text = "$quantity",
            modifier = Modifier.padding(horizontal = 12.dp),
            style = MaterialTheme.typography.titleMedium,
        )
        FilledTonalIconButton(onClick = onIncrement, modifier = Modifier.size(32.dp)) { Text("+") }
    }
}

@Composable
fun ProductCard(product: Product, onAdd: () -> Unit, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = MaterialTheme.shapes.medium) {
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(1.3f)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            if (product.imageUrl != null) {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                // Emoji placeholder until real images are added.
                Text(text = foodEmoji(product.name), fontSize = 56.sp)
            }
        }
        Column(Modifier.padding(12.dp)) {
            Text(
                text = product.name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PriceText(product.priceCents)
                AppButton(stringResource(R.string.ui_add), onAdd, variant = AppButtonVariant.Tonal)
            }
        }
    }
}

@Composable
fun CartLineRow(item: CartLine, onDecrement: () -> Unit, onIncrement: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(item.name, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
        QuantityStepper(item.quantity, onDecrement, onIncrement)
        // Fixed-width, end-aligned price so a longer total never shifts the +/- buttons.
        Box(Modifier.width(88.dp).padding(start = 8.dp), contentAlignment = Alignment.CenterEnd) {
            PriceText(item.lineTotalCents)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosTopBar(
    title: String,
    onSettingsClick: () -> Unit,
    language: String,
    onToggleLanguage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        title = { Text(title, fontWeight = FontWeight.Bold) },
        actions = {
            TextButton(onClick = onToggleLanguage) {
                Text(language, fontWeight = FontWeight.Bold)
            }
            IconButton(onClick = onSettingsClick) {
                Icon(Icons.Filled.Settings, contentDescription = stringResource(R.string.ui_settings))
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ),
        modifier = modifier,
    )
}

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

/** Emoji stand-in for a product image, chosen from keywords in the name. */
fun foodEmoji(name: String): String {
    val n = name.lowercase()
    return when {
        "espresso" in n || "latte" in n || "cappuccino" in n || "coffee" in n -> "☕"
        "tea" in n -> "🍵"
        "croissant" in n -> "🥐"
        "muffin" in n || "cake" in n || "dessert" in n -> "🧁"
        "cookie" in n -> "🍪"
        "water" in n -> "💧"
        "juice" in n || "soda" in n || "drink" in n -> "🥤"
        "sandwich" in n -> "🥪"
        "burger" in n -> "🍔"
        "pizza" in n -> "🍕"
        "salad" in n -> "🥗"
        "fries" in n -> "🍟"
        else -> "🍽"
    }
}
