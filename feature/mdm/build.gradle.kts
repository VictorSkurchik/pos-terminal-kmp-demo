plugins {
    id("posterminal.android.library")
    id("posterminal.android.compose")
    id("posterminal.quality")
}

android {
    namespace = "by.vsdev.posterminal.demo.feature.mdm"
}

dependencies {
    implementation(projects.core.domain)
    implementation(projects.core.ui)

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.lifecycle.viewmodelCompose)
    implementation(libs.androidx.work.runtime)
    implementation(libs.koin.android)
    implementation(libs.koin.androidxCompose)
    implementation(libs.koin.androidxWorkmanager)

    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.mlkit.barcodeScanning)

    testImplementation(libs.kotlin.testJunit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.mockk)
}
