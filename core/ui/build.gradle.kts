plugins {
    id("posterminal.android.library")
    id("posterminal.android.compose")
    id("posterminal.quality")
}

android {
    namespace = "by.vsdev.posterminal.demo.core.ui"
}

dependencies {
    // Generic design system + MVI base — no domain, no feature-specific deps.
    api(libs.compose.runtime)
    api(libs.compose.foundation)
    api(libs.compose.material3)
    api(libs.compose.materialIconsCore)
    api(libs.compose.ui)
    api(libs.androidx.lifecycle.runtimeCompose)
    api(libs.androidx.lifecycle.viewmodelCompose)
}
