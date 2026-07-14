package by.vsdev.posterminal.demo.feature.mdm

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.koinInject

/** Shows a dialog with an admin message (the SHOW_MESSAGE command). Placed at the UI root. */
@Composable
fun MdmMessageHost(controller: MdmController = koinInject()) {
    val message by controller.message.collectAsStateWithLifecycle()
    message?.let { text ->
        AlertDialog(
            onDismissRequest = { controller.consumeMessage() },
            confirmButton = {
                TextButton(onClick = { controller.consumeMessage() }) { Text("OK") }
            },
            title = { Text("Message from admin") },
            text = { Text(text) },
        )
    }
}
