plugins {
    id("posterminal.android.library")
    id("posterminal.android.compose")
    id("posterminal.quality")
}

android {
    namespace = "by.vsdev.posterminal.demo.core.ui"
}

dependencies {
    api(projects.core.domain)

    api(libs.compose.runtime)
    api(libs.compose.foundation)
    api(libs.compose.material3)
    api(libs.compose.materialIconsCore)
    api(libs.compose.ui)
    api(libs.androidx.lifecycle.runtimeCompose)
    api(libs.androidx.lifecycle.viewmodelCompose)

    api(libs.coil.compose)
    implementation(libs.coil.networkOkhttp)
}
