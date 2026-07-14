package by.vsdev.posterminal.demo.feature.mdm.enrollment

import android.Manifest
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import by.vsdev.posterminal.demo.core.ui.components.AppButton
import by.vsdev.posterminal.demo.core.ui.components.AppButtonVariant
import org.koin.androidx.compose.koinViewModel

/**
 * Initial screen shown until the terminal is enrolled. Register by scanning the QR from the admin
 * console (or a manual fallback). On success `enrolled` flips true and AppNavHost routes to POS.
 */
@Composable
fun RegistrationScreen(
    modifier: Modifier = Modifier,
    viewModel: EnrollmentViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
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
                    viewModel.enrollWithToken(payload)
                },
                modifier = Modifier.fillMaxSize(),
            )
            AppButton(
                text = "Cancel",
                onClick = { showScanner = false },
                variant = AppButtonVariant.Tonal,
                modifier = Modifier.align(Alignment.BottomCenter).padding(24.dp),
            )
        }
        return
    }

    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.fillMaxSize().systemBarsPadding().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                "🍽",
                style = MaterialTheme.typography.displaySmall,
            )
            Text(
                "Restaurant POS",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp),
            )
            Text(
                "Set up this terminal by scanning the enrollment QR from the admin console.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp, bottom = 32.dp),
            )

            AppButton(
                text = "Scan QR to register",
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
                onClick = viewModel::enroll,
                enabled = !state.busy,
                modifier = Modifier.padding(top = 8.dp),
            ) { Text("Register manually") }

            if (state.busy) {
                CircularProgressIndicator(Modifier.padding(top = 24.dp).size(28.dp))
            }
            state.status?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 24.dp),
                )
            }
        }
    }
}
