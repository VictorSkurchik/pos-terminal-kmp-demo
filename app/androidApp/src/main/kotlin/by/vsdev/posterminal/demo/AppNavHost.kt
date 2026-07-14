package by.vsdev.posterminal.demo

import android.os.SystemClock
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import by.vsdev.posterminal.demo.core.data.prefs.SettingsRepository
import by.vsdev.posterminal.demo.feature.mdm.MdmController
import by.vsdev.posterminal.demo.feature.mdm.MdmMessageHost
import by.vsdev.posterminal.demo.feature.mdm.enrollment.RegistrationScreen
import by.vsdev.posterminal.demo.feature.mdm.enrollment.SettingsScreen
import by.vsdev.posterminal.demo.feature.offer.OfferScreen
import by.vsdev.posterminal.demo.feature.pos.PosScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import org.koin.compose.koinInject

object Routes {
    const val REGISTRATION = "registration"
    const val POS = "pos"
    const val SETTINGS = "settings"
    const val OFFER = "offer"
}

private const val KIOSK_IDLE_MILLIS = 10_000L

@Composable
fun AppNavHost(
    settings: SettingsRepository = koinInject(),
    controller: MdmController = koinInject(),
) {
    val enrolled by produceState<Boolean?>(initialValue = null) {
        settings.enrolled.collect { value = it }
    }

    val commandSnackbar = remember { SnackbarHostState() }
    LaunchedEffect(Unit) { controller.events.collect { commandSnackbar.showSnackbar(it) } }

    val resolved = enrolled ?: run {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }

    val nav = rememberNavController()
    val kioskActive by controller.kioskActive.collectAsStateWithLifecycle()
    var lastInteraction by remember { mutableLongStateOf(SystemClock.elapsedRealtime()) }

    // Enrolment changes: enroll → POS; logout / wipe (auto un-enroll on 404) → Registration.
    LaunchedEffect(nav) {
        settings.enrolled.drop(1).distinctUntilChanged().collect { isEnrolled ->
            val target = if (isEnrolled) Routes.POS else Routes.REGISTRATION
            if (nav.currentDestination?.route != target) {
                nav.navigate(target) {
                    popUpTo(nav.graph.id) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
    }

    // Kiosk screensaver: while kiosk is on, show the Offer attract loop after 10 s of inactivity.
    LaunchedEffect(kioskActive) {
        if (!kioskActive) {
            if (nav.currentDestination?.route == Routes.OFFER) nav.popBackStack()
            return@LaunchedEffect
        }
        lastInteraction = SystemClock.elapsedRealtime()
        while (true) {
            delay(1_000)
            val idle = SystemClock.elapsedRealtime() - lastInteraction
            if (idle >= KIOSK_IDLE_MILLIS && nav.currentDestination?.route == Routes.POS) {
                nav.navigate(Routes.OFFER) { launchSingleTop = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            // Observe every touch (Initial pass, without consuming) to reset the idle timer.
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        awaitPointerEvent(PointerEventPass.Initial)
                        lastInteraction = SystemClock.elapsedRealtime()
                    }
                }
            },
    ) {
        NavHost(
            navController = nav,
            startDestination = if (resolved) Routes.POS else Routes.REGISTRATION,
            modifier = Modifier.fillMaxSize(),
        ) {
            composable(Routes.REGISTRATION) { RegistrationScreen() }
            composable(Routes.POS) { PosScreen(onOpenSettings = { nav.navigate(Routes.SETTINGS) }) }
            composable(Routes.SETTINGS) { SettingsScreen(onBack = { nav.popBackStack() }) }
            composable(Routes.OFFER) { OfferScreen(onExit = { nav.popBackStack() }) }
        }
        SnackbarHost(commandSnackbar, Modifier.align(Alignment.BottomCenter).navigationBarsPadding())
        MdmMessageHost()
    }
}
