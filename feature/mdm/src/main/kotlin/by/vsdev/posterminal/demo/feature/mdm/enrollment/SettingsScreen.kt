package by.vsdev.posterminal.demo.feature.mdm.enrollment

import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import by.vsdev.posterminal.demo.core.ui.components.AppButton
import by.vsdev.posterminal.demo.core.ui.components.AppButtonVariant
import by.vsdev.posterminal.demo.core.ui.components.ConfirmDialog
import by.vsdev.posterminal.demo.core.ui.components.InfoRow
import by.vsdev.posterminal.demo.core.ui.components.SectionTitle
import by.vsdev.posterminal.demo.core.ui.theme.PosTheme
import by.vsdev.posterminal.demo.feature.mdm.R
import by.vsdev.posterminal.demo.feature.mdm.admin.PosDeviceAdminReceiver
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val context = LocalContext.current

    val adminLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        viewModel.onIntent(SettingsIntent.AdminResult)
    }

    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                SettingsSideEffect.LaunchDeviceAdmin -> adminLauncher.launch(addAdminIntent(context))
                else -> snackbar.showSnackbar(effect.toMessage(context))
            }
        }
    }

    SettingsContent(
        state = state,
        snackbar = snackbar,
        onBack = onBack,
        onIntent = viewModel::onIntent,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsContent(
    state: SettingsUiState,
    snackbar: SnackbarHostState,
    onBack: () -> Unit,
    onIntent: (SettingsIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    var confirmReset by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.settings_back),
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
        ) {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    SectionTitle(stringResource(R.string.settings_section_device))
                    InfoRow(stringResource(R.string.settings_device_id), state.deviceId)
                    InfoRow(stringResource(R.string.settings_name), state.name)
                    InfoRow(stringResource(R.string.settings_enrolled), yesNo(state.enrolled))
                    InfoRow(stringResource(R.string.settings_device_admin), activeInactive(state.adminActive))
                    InfoRow(stringResource(R.string.settings_kiosk), onOff(state.kioskActive))
                }
            }

            Spacer(Modifier.height(12.dp))
            AppButton(
                text = stringResource(
                    if (state.adminActive) R.string.settings_admin_enabled else R.string.settings_enable_admin,
                ),
                onClick = { onIntent(SettingsIntent.EnableAdmin) },
                variant = AppButtonVariant.Outlined,
                enabled = !state.adminActive,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            AppButton(
                text = stringResource(R.string.settings_sync_now),
                onClick = { onIntent(SettingsIntent.SyncNow) },
                variant = AppButtonVariant.Tonal,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.weight(1f))
            AppButton(
                text = stringResource(R.string.settings_factory_reset),
                onClick = { confirmReset = true },
                variant = AppButtonVariant.Danger,
                enabled = !state.busy,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    if (confirmReset) {
        ConfirmDialog(
            title = stringResource(R.string.settings_reset_title),
            text = stringResource(R.string.settings_reset_text),
            confirmLabel = stringResource(R.string.settings_factory_reset),
            danger = true,
            onConfirm = {
                confirmReset = false
                onIntent(SettingsIntent.FactoryReset)
            },
            onDismiss = { confirmReset = false },
        )
    }
}

@Composable
private fun yesNo(value: Boolean) =
    stringResource(if (value) R.string.settings_yes else R.string.settings_no)

@Composable
private fun activeInactive(value: Boolean) =
    stringResource(if (value) R.string.settings_active else R.string.settings_inactive)

@Composable
private fun onOff(value: Boolean) =
    stringResource(if (value) R.string.settings_on else R.string.settings_off)

private fun addAdminIntent(context: Context): Intent =
    Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
        .putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, PosDeviceAdminReceiver.componentName(context))
        .putExtra(
            DevicePolicyManager.EXTRA_ADD_EXPLANATION,
            context.getString(R.string.settings_admin_explanation),
        )

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun SettingsPreview() {
    PosTheme {
        SettingsContent(
            state = SettingsUiState(
                deviceId = "pos-1a2b3c4d",
                name = "Front Till",
                enrolled = true,
                adminActive = true,
            ),
            snackbar = remember { SnackbarHostState() },
            onBack = {},
            onIntent = {},
        )
    }
}
