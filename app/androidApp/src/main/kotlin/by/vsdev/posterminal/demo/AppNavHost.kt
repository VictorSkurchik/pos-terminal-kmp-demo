package by.vsdev.posterminal.demo

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import by.vsdev.posterminal.demo.feature.mdm.MdmMessageHost
import by.vsdev.posterminal.demo.feature.mdm.enrollment.RegistrationScreen
import by.vsdev.posterminal.demo.feature.mdm.enrollment.SettingsScreen
import by.vsdev.posterminal.demo.feature.offer.OfferScreen
import by.vsdev.posterminal.demo.feature.pos.PosScreen
import org.koin.androidx.compose.koinViewModel

@Composable
fun AppNavHost(appViewModel: AppViewModel = koinViewModel()) {
    val startRoute by appViewModel.startRoute.collectAsStateWithLifecycle()

    val route = startRoute ?: run {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }

    val nav = rememberNavController()

    LaunchedEffect(Unit) {
        appViewModel.navEvents.collect { event -> nav.handle(event) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            // Observe every touch (Initial pass, without consuming) to reset the idle timer.
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        awaitPointerEvent(PointerEventPass.Initial)
                        appViewModel.onUserInteraction()
                    }
                }
            },
    ) {
        NavHost(navController = nav, startDestination = route, modifier = Modifier.fillMaxSize()) {
            composable<AppRoute.Registration> { RegistrationScreen() }
            composable<AppRoute.Pos> { PosScreen(onOpenSettings = { nav.navigate(AppRoute.Settings) }) }
            composable<AppRoute.Settings> { SettingsScreen(onBack = { nav.popBackStack() }) }
            composable<AppRoute.Offer> { OfferScreen(onExit = { nav.popBackStack() }) }
        }
        MdmMessageHost()
    }
}

private fun NavController.handle(event: AppNavEvent) {
    when (event) {
        is AppNavEvent.Reset -> navigate(event.route) {
            popUpTo(graph.id) { inclusive = true }
            launchSingleTop = true
        }

        AppNavEvent.ShowOffer -> {
            val onPos = currentDestination?.hasRoute(AppRoute.Pos::class) == true
            if (onPos) navigate(AppRoute.Offer) { launchSingleTop = true }
        }

        AppNavEvent.DismissOffer -> {
            val onOffer = currentDestination?.hasRoute(AppRoute.Offer::class) == true
            if (onOffer) popBackStack()
        }
    }
}
