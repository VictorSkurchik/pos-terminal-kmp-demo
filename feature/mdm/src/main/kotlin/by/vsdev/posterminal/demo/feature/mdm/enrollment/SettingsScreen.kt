package by.vsdev.posterminal.demo.feature.mdm.enrollment

import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import by.vsdev.posterminal.demo.core.ui.components.AppButton
import by.vsdev.posterminal.demo.core.ui.components.AppButtonVariant
import by.vsdev.posterminal.demo.core.ui.components.ConfirmDialog
import by.vsdev.posterminal.demo.core.ui.components.InfoRow
import by.vsdev.posterminal.demo.core.ui.components.SectionTitle
import by.vsdev.posterminal.demo.feature.mdm.admin.PosDeviceAdminReceiver
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
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
    var confirmLogout by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = { IconButton(onClick = onBack) { Text("←") } },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    SectionTitle("Device")
                    InfoRow("Device ID", state.deviceId)
                    InfoRow("Name", state.name)
                    InfoRow("Enrolled", if (state.enrolled) "yes" else "no")
                    InfoRow("Device Admin", if (adminActive) "active" else "inactive")
                    InfoRow("Kiosk (pinning)", if (kioskActive) "on" else "off")
                }
            }

            AppButton(
                text = if (adminActive) "Device Admin enabled" else "Enable Device Admin",
                onClick = { adminLauncher.launch(addAdminIntent(context)) },
                variant = AppButtonVariant.Outlined,
                enabled = !adminActive,
                modifier = Modifier.fillMaxWidth(),
            )
            AppButton(
                text = "Sync now",
                onClick = viewModel::syncNow,
                variant = AppButtonVariant.Tonal,
                modifier = Modifier.fillMaxWidth(),
            )
            AppButton(
                text = "Logout",
                onClick = { confirmLogout = true },
                variant = AppButtonVariant.Danger,
                enabled = !state.busy,
                modifier = Modifier.fillMaxWidth(),
            )

            state.status?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
        }
    }

    if (confirmLogout) {
        ConfirmDialog(
            title = "Log out this terminal?",
            text = "It will be removed from the admin console and reset to the registration screen.",
            confirmLabel = "Log out",
            danger = true,
            onConfirm = {
                confirmLogout = false
                viewModel.logout()
            },
            onDismiss = { confirmLogout = false },
        )
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
