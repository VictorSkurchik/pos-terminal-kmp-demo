package by.vsdev.posterminal.demo.feature.mdm

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import org.koin.compose.koinInject

/**
 * Shows a dialog for admin messages (the SHOW_MESSAGE command). Collects the controller's one-shot
 * [MdmController.messages] events into local dialog state, so each message shows exactly once.
 * Placed at the UI root.
 */
@Composable
fun MdmMessageHost(controller: MdmController = koinInject()) {
    var current by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        controller.messages.collect { current = it }
    }

    current?.let { text ->
        AlertDialog(
            onDismissRequest = { current = null },
            confirmButton = {
                TextButton(onClick = { current = null }) { Text(stringResource(R.string.msg_ok)) }
            },
            title = { Text(stringResource(R.string.msg_from_admin)) },
            text = { Text(text) },
        )
    }
}
