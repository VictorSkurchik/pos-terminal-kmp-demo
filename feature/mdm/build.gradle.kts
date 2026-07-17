plugins {
    id("posterminal.android.library")
    id("posterminal.android.compose")
    id("posterminal.quality")
}

android {
    namespace = "by.vsdev.posterminal.demo.feature.mdm"
}

dependencies {
    implementation(projects.core)
    implementation(projects.core.domain)
    implementation(projects.core.ui)

    implementation(platform(libs.koin.bom))
    implementation(platform(libs.kotlinx.coroutines.bom))
    implementation(platform(libs.ktor.bom))

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.lifecycle.viewmodelCompose)
    implementation(libs.androidx.work.runtime)
    implementation(libs.koin.android)
    implementation(libs.koin.androidxCompose)
    implementation(libs.koin.androidxWorkmanager)

    // Backend transport (device repo + error mapping) and local enrollment settings.
    implementation(libs.ktor.clientCore)
    implementation(libs.ktor.serializationJson)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.datastore.preferences)

    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.mlkit.barcodeScanning)

    testImplementation(libs.kotlin.testJunit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.mockk)
    testImplementation(libs.ktor.clientMock)
    testImplementation(libs.ktor.clientContentNegotiation)
}
