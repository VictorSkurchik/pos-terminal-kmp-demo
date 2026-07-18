plugins {
    id("posterminal.android.library")
    id("posterminal.android.compose")
    id("posterminal.quality")
    id("org.jetbrains.kotlin.plugin.parcelize")
}

android {
    namespace = "by.vsdev.posterminal.demo.feature.offer"
}

dependencies {
    implementation(projects.core.ui)

    implementation(platform(libs.koin.bom))

    implementation(libs.androidx.lifecycle.viewmodelCompose)
    implementation(libs.koin.android)
    implementation(libs.koin.androidxCompose)
}
