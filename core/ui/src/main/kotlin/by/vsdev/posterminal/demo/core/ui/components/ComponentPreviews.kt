package by.vsdev.posterminal.demo.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import by.vsdev.posterminal.demo.core.ui.theme.PosTheme

@Preview(name = "Buttons — light")
@Preview(name = "Buttons — dark", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AppButtonPreview() {
    PosTheme {
        Surface {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AppButton("Primary", {}, variant = AppButtonVariant.Primary)
                AppButton("Tonal", {}, variant = AppButtonVariant.Tonal)
                AppButton("Outlined", {}, variant = AppButtonVariant.Outlined)
                AppButton("Danger", {}, variant = AppButtonVariant.Danger)
            }
        }
    }
}
