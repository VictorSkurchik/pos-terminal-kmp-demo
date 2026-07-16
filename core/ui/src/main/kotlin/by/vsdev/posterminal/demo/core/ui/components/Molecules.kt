package by.vsdev.posterminal.demo.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import by.vsdev.posterminal.demo.core.ui.R
import by.vsdev.posterminal.demo.domain.model.CartLine
import by.vsdev.posterminal.demo.domain.model.Product
import coil3.compose.AsyncImage

// ---------- Molecules ----------

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
fun CartLineRow(
    item: CartLine,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    modifier: Modifier = Modifier,
) {
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
fun PosTopBar(title: String, onSettingsClick: () -> Unit, modifier: Modifier = Modifier) {
    TopAppBar(
        title = { Text(title, fontWeight = FontWeight.Bold) },
        actions = {
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
fun InfoRow(label: String, value: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun ConfirmDialog(
    title: String,
    text: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmLabel: String? = null,
    dismissLabel: String? = null,
    danger: Boolean = false,
) {
    val confirm = confirmLabel ?: stringResource(android.R.string.ok)
    val dismiss = dismissLabel ?: stringResource(android.R.string.cancel)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(text) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    confirm,
                    color = if (danger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                )
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(dismiss) } },
    )
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
