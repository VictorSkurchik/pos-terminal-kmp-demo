package by.vsdev.posterminal.demo.feature.mdm.enrollment

import android.Manifest
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import by.vsdev.posterminal.demo.feature.mdm.admin.PosDeviceAdminReceiver
import org.koin.androidx.compose.koinViewModel

@Composable
fun EnrollmentScreen(
    modifier: Modifier = Modifier,
    viewModel: EnrollmentViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val kioskActive by viewModel.kioskActive.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var adminActive by remember { mutableStateOf(isAdminActive(context)) }
    val adminLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        adminActive = isAdminActive(context)
    }

    var showScanner by remember { mutableStateOf(false) }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        showScanner = granted
    }

    if (showScanner) {
        Box(modifier.fillMaxSize()) {
            QrScanner(
                onResult = { payload ->
                    showScanner = false
                    viewModel.enrollWithToken(payload)
                },
                modifier = Modifier.fillMaxSize(),
            )
            Button(
                onClick = { showScanner = false },
                modifier = Modifier.align(Alignment.BottomCenter).padding(24.dp),
            ) { Text("Cancel") }
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Device management", style = MaterialTheme.typography.headlineSmall)

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Device ID: ${state.deviceId}", style = MaterialTheme.typography.bodyMedium)
                Text("Enrolled: ${if (state.enrolled) "yes" else "no"}")
                Text("Device Admin: ${if (adminActive) "active" else "inactive"}")
                Text("Kiosk (pinning): ${if (kioskActive) "on" else "off"}")
            }
        }

        OutlinedTextField(
            value = state.name,
            onValueChange = viewModel::onNameChange,
            label = { Text("Display name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        Button(
            onClick = viewModel::enroll,
            enabled = !state.busy,
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (state.busy) {
                CircularProgressIndicator(Modifier.padding(end = 8.dp))
            }
            Text(if (state.enrolled) "Re-register" else "Enroll")
        }

        OutlinedButton(
            onClick = {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED
                ) {
                    showScanner = true
                } else {
                    cameraLauncher.launch(Manifest.permission.CAMERA)
                }
            },
            enabled = !state.busy,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Scan QR to enroll")
        }

        OutlinedButton(onClick = viewModel::syncNow, modifier = Modifier.fillMaxWidth()) {
            Text("Sync now")
        }

        OutlinedButton(
            onClick = { adminLauncher.launch(addAdminIntent(context)) },
            enabled = !adminActive,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (adminActive) "Device Admin enabled" else "Enable Device Admin")
        }

        state.status?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
    }
}

private fun isAdminActive(context: Context): Boolean {
    val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    return dpm.isAdminActive(PosDeviceAdminReceiver.componentName(context))
}

private fun addAdminIntent(context: Context): Intent =
    Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
        .putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, PosDeviceAdminReceiver.componentName(context))
        .putExtra(
            DevicePolicyManager.EXTRA_ADD_EXPLANATION,
            "Required so the admin can remotely lock this POS terminal.",
        )
