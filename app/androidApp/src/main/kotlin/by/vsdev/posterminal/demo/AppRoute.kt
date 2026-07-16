package by.vsdev.posterminal.demo

import kotlinx.serialization.Serializable

/** Type-safe Navigation-Compose routes. */
sealed interface AppRoute {
    @Serializable
    data object Registration : AppRoute

    @Serializable
    data object Pos : AppRoute

    @Serializable
    data object Settings : AppRoute

    @Serializable
    data object Offer : AppRoute
}

/** Navigation intents emitted by [AppViewModel] and performed by the NavHost. */
sealed interface AppNavEvent {
    /** Clear the back stack and land on [route] (enrollment / logout transitions). */
    data class Reset(val route: AppRoute) : AppNavEvent

    /** Kiosk idle: show the Offer attract loop. */
    data object ShowOffer : AppNavEvent

    /** Kiosk turned off: leave the Offer attract loop. */
    data object DismissOffer : AppNavEvent
}
