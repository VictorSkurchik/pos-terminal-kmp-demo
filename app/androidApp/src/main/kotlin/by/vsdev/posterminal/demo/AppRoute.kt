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
