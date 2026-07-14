package by.vsdev.posterminal.demo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import by.vsdev.posterminal.demo.dto.NewCommandRequest
import by.vsdev.posterminal.demo.model.CommandType
import by.vsdev.posterminal.demo.model.Device
import by.vsdev.posterminal.demo.network.PosApiClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val SERVER_URL = "https://pos-terminal-kmp-demo.onrender.com"

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdminApp() {
    val api = remember { PosApiClient(SERVER_URL) }
    val scope = rememberCoroutineScope()

    var devices by remember { mutableStateOf<List<Device>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    var messageText by remember { mutableStateOf("Please restart the terminal") }

    LaunchedEffect(Unit) {
        while (true) {
            try {
                devices = api.listDevices()
                error = null
            } catch (e: Exception) {
                error = e.message ?: "request failed"
            }
            delay(3000)
        }
    }

    fun send(device: Device, type: CommandType, payload: String? = null) {
        scope.launch {
            try {
                api.postCommand(device.id, NewCommandRequest(type, payload))
            } catch (e: Exception) {
                error = e.message
            }
        }
    }

    MaterialTheme {
        Column(Modifier.fillMaxSize().padding(24.dp)) {
            Text("POS MDM — Admin Console", style = MaterialTheme.typography.headlineMedium)
            Text(
                "Server: $SERVER_URL · auto-refresh 3s",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 12.dp),
            )
            error?.let {
                Text("Error: $it", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(bottom = 8.dp))
            }

            EnrollmentQrCard(serverUrl = SERVER_URL)

            OutlinedTextField(
                value = messageText,
                onValueChange = { messageText = it },
                label = { Text("SHOW_MESSAGE text") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            )

            if (devices.isEmpty()) {
                Text("No devices enrolled yet.")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(devices, key = { it.id }) { device ->
                        DeviceCard(device, messageText, ::send)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DeviceCard(
    device: Device,
    messageText: String,
    onSend: (Device, CommandType, String?) -> Unit,
) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(device.name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Text(
                "id=${device.id} · status=${device.status} · battery=${device.batteryLevel ?: "?"}%" +
                    (device.enrollmentToken?.let { " · enrolled via QR token=$it" } ?: ""),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onSend(device, CommandType.LOCK, null) }) { Text("Lock") }
                Button(onClick = { onSend(device, CommandType.KIOSK_ON, null) }) { Text("Kiosk On") }
                OutlinedButton(onClick = { onSend(device, CommandType.KIOSK_OFF, null) }) { Text("Kiosk Off") }
                Button(onClick = { onSend(device, CommandType.SHOW_MESSAGE, messageText) }) { Text("Message") }
                Button(onClick = { onSend(device, CommandType.RESTRICT_APP, "on") }) { Text("Restrict") }
                OutlinedButton(onClick = { onSend(device, CommandType.RESTRICT_APP, "off") }) { Text("Unrestrict") }
                Button(onClick = { onSend(device, CommandType.WIPE, null) }) { Text("Wipe") }
            }
        }
    }
}
