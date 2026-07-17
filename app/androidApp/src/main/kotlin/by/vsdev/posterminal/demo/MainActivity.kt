package by.vsdev.posterminal.demo

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import by.vsdev.posterminal.demo.core.ui.theme.PosTheme
import by.vsdev.posterminal.demo.feature.mdm.MdmAgentService
import by.vsdev.posterminal.demo.feature.mdm.MdmController
import by.vsdev.posterminal.demo.locale.AppLocale
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val mdmController: MdmController by inject()

    private val notificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    // Apply the selected app language by wrapping the base context.
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(AppLocale.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        MdmAgentService.start(this)

        setContent {
            PosTheme {
                AppNavHost(
                    language = AppLocale.current(this).uppercase(),
                    onToggleLanguage = {
                        AppLocale.toggle(this)
                        recreate()
                    },
                )
            }
        }
    }

    // Bind the current Activity to the controller — required for kiosk (startLockTask).
    override fun onResume() {
        super.onResume()
        mdmController.bind(this)
    }

    override fun onPause() {
        mdmController.unbind(this)
        super.onPause()
    }
}
