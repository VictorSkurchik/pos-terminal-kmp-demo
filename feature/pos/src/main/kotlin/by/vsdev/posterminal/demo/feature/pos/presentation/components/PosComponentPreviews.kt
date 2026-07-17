package by.vsdev.posterminal.demo.feature.pos.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import by.vsdev.posterminal.demo.core.ui.theme.PosTheme
import by.vsdev.posterminal.demo.feature.pos.domain.model.CartLine
import by.vsdev.posterminal.demo.feature.pos.domain.model.Product

@Preview
@Composable
private fun ProductCardPreview() {
    PosTheme {
        Surface {
            Column(Modifier.padding(16.dp).width(180.dp)) {
                ProductCard(Product("sku-latte", "Latte", 450), onAdd = {})
            }
        }
    }
}

@Preview(name = "Cart — light")
@Preview(name = "Cart — dark", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CartPanelPreview() {
    PosTheme {
        CartPanel(
            items = listOf(
                CartLine("sku-espresso", "Espresso", 300, 1),
                CartLine("sku-latte", "Latte", 450, 2),
            ),
            totalCents = 1200,
            payEnabled = true,
            payLabel = "Pay",
            onIncrement = {},
            onDecrement = {},
            onPay = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
