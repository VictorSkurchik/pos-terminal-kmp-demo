package by.vsdev.posterminal.demo

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import by.vsdev.posterminal.demo.dto.EnrollmentToken
import by.vsdev.posterminal.demo.dto.toQrPayload
import io.github.alexzhirkevich.qrose.rememberQrCodePainter
import kotlin.random.Random

/** Generates a QR with an [EnrollmentToken] — the device scans it and self-registers. */
@Composable
fun EnrollmentQrCard(serverUrl: String) {
    var token by remember { mutableStateOf(newToken()) }
    val payload = remember(token) { EnrollmentToken(token = token, serverUrl = serverUrl).toQrPayload() }
    val painter = rememberQrCodePainter(payload)

    Card(Modifier.padding(bottom = 16.dp)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Image(painter = painter, contentDescription = "Enrollment QR", modifier = Modifier.size(160.dp))
            Column(Modifier.padding(start = 16.dp)) {
                Text("QR enrollment", style = MaterialTheme.typography.titleMedium)
                Text("token: $token", style = MaterialTheme.typography.bodySmall)
                Text("Scan in the app: Manage → Scan QR", style = MaterialTheme.typography.bodySmall)
                OutlinedButton(onClick = { token = newToken() }, modifier = Modifier.padding(top = 8.dp)) {
                    Text("New token")
                }
            }
        }
    }
}

private fun newToken(): String = "enr-" + Random.nextInt(0x10000, 0xFFFFF).toString(16)
