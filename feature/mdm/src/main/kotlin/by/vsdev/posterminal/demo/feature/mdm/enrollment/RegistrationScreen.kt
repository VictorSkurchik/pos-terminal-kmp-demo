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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import by.vsdev.posterminal.demo.core.ui.components.AppButton
import by.vsdev.posterminal.demo.core.ui.components.AppButtonVariant
import by.vsdev.posterminal.demo.core.ui.theme.PosTheme
import by.vsdev.posterminal.demo.feature.mdm.R
import by.vsdev.posterminal.demo.feature.mdm.admin.PosDeviceAdminReceiver
import org.koin.androidx.compose.koinViewModel

/**
 * Initial screen shown until the terminal is enrolled. The user registers (QR or manual fallback),
 * then MUST enable Device Admin before enrollment completes and AppNavHost routes to POS.
 */
@Composable
fun RegistrationScreen(modifier: Modifier = Modifier, viewModel: RegistrationViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val context = LocalContext.current

    val adminLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        viewModel.onIntent(RegistrationIntent.AdminResult)
    }

    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                RegistrationSideEffect.LaunchDeviceAdmin -> adminLauncher.launch(addAdminIntent(context))
                else -> snackbar.showSnackbar(effect.toMessage(context))
            }
        }
    }

    RegistrationContent(
        state = state,
        snackbar = snackbar,
        onIntent = viewModel::onIntent,
        modifier = modifier,
    )
}

@Composable
private fun RegistrationContent(
    state: RegistrationUiState,
    snackbar: SnackbarHostState,
    onIntent: (RegistrationIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var showScanner by remember { mutableStateOf(false) }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        showScanner = granted
    }

    if (showScanner) {
        Box(modifier.fillMaxSize()) {
            QrScanner(
                onResult = { payload ->
                    showScanner = false
                    onIntent(RegistrationIntent.RegisterWithToken(payload))
                },
                modifier = Modifier.fillMaxSize(),
            )
            AppButton(
                text = stringResource(R.string.reg_cancel),
                onClick = { showScanner = false },
                variant = AppButtonVariant.Tonal,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(24.dp),
            )
        }
        return
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .systemBarsPadding()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(stringResource(R.string.reg_brand_emoji), style = MaterialTheme.typography.displaySmall)
            Text(
                stringResource(R.string.reg_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp),
            )
            Text(
                stringResource(if (state.awaitingAdmin) R.string.reg_admin_hint else R.string.reg_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp, bottom = 32.dp),
            )

            if (state.awaitingAdmin) {
                // Device Admin gate: enrollment only finishes (→ POS) once admin is granted.
                AppButton(
                    text = stringResource(R.string.settings_enable_admin),
                    onClick = { onIntent(RegistrationIntent.EnableAdmin) },
                    enabled = !state.busy,
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                AppButton(
                    text = stringResource(R.string.reg_scan_qr),
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
                )

                TextButton(
                    onClick = { onIntent(RegistrationIntent.RegisterManually) },
                    enabled = !state.busy,
                    modifier = Modifier.padding(top = 8.dp),
                ) { Text(stringResource(R.string.reg_register_manually)) }
            }

            if (state.busy) {
                CircularProgressIndicator(
                    Modifier
                        .padding(top = 24.dp)
                        .size(28.dp),
                )
            }
        }
    }
}

private fun addAdminIntent(context: Context): Intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
    .putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, PosDeviceAdminReceiver.componentName(context))
    .putExtra(
        DevicePolicyManager.EXTRA_ADD_EXPLANATION,
        context.getString(R.string.settings_admin_explanation),
    )

@Preview
@Composable
private fun RegistrationPreview() {
    PosTheme {
        RegistrationContent(
            state = RegistrationUiState(deviceId = "pos-1a2b3c4d", name = "Front Till"),
            snackbar = remember { SnackbarHostState() },
            onIntent = {},
        )
    }
}
