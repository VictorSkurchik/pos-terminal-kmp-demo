package by.vsdev.posterminal.demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import by.vsdev.posterminal.demo.core.ui.theme.PosTheme
import by.vsdev.posterminal.demo.feature.mdm.CommandFeedViewModel
import by.vsdev.posterminal.demo.feature.mdm.MdmController
import by.vsdev.posterminal.demo.feature.mdm.MdmMessageHost
import by.vsdev.posterminal.demo.feature.mdm.enrollment.EnrollmentScreen
import by.vsdev.posterminal.demo.feature.pos.PosScreen
import org.koin.android.ext.android.inject
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {

    private val mdmController: MdmController by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            PosTheme {
                AppRoot()
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

@Composable
private fun AppRoot(feed: CommandFeedViewModel = koinViewModel()) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Live command feed: poll while the app is on screen, show each command as a snackbar.
    LaunchedEffect(Unit) { feed.poll() }
    LaunchedEffect(Unit) {
        feed.snackbar.collect { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().systemBarsPadding(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            PrimaryTabRow(selectedTabIndex = tab) {
                Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("POS") })
                Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Manage") })
            }
            Box(Modifier.weight(1f)) {
                when (tab) {
                    0 -> PosScreen()
                    else -> EnrollmentScreen()
                }
            }
        }
    }
    MdmMessageHost()
}
