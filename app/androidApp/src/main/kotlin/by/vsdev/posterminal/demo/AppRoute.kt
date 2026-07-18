package by.vsdev.posterminal.demo

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/** Type-safe Navigation-Compose routes; also [Parcelable] so [AppUiState] can be saved. */
sealed interface AppRoute : Parcelable {
    @Serializable
    @Parcelize
    data object Registration : AppRoute

    @Serializable
    @Parcelize
    data object Pos : AppRoute

    @Serializable
    @Parcelize
    data object Settings : AppRoute

    @Serializable
    @Parcelize
    data object Offer : AppRoute
}
