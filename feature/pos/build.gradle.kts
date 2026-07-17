plugins {
    id("posterminal.android.library")
    id("posterminal.android.compose")
    id("posterminal.quality")
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

    testImplementation(libs.kotlin.testJunit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
}
