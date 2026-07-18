plugins {
    id("posterminal.android.library")
    id("posterminal.android.compose")
    alias(libs.plugins.ksp)
    id("posterminal.quality")
    id("org.jetbrains.kotlin.plugin.parcelize")
}

android {
    namespace = "by.vsdev.posterminal.demo.feature.pos"
}

dependencies {
    implementation(projects.core.domain)
    implementation(projects.core.ui)

    implementation(platform(libs.koin.bom))
    implementation(platform(libs.kotlinx.coroutines.bom))

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.lifecycle.viewmodelCompose)
    implementation(libs.koin.android)
    implementation(libs.koin.androidxCompose)

    // Cart persistence (this feature owns its Room store).
    implementation(libs.room.runtime)
    implementation(libs.sqlite.bundled)
    ksp(libs.room.compiler)

    // Product/offer imagery (moved here with the POS product card).
    implementation(platform(libs.coil.bom))
    implementation(libs.coil.compose)
    implementation(libs.coil.networkOkhttp)

    testImplementation(libs.kotlin.testJunit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
}
