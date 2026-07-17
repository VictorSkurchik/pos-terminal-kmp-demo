plugins {
    alias(libs.plugins.kotlinJvm)
    id("posterminal.quality")
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(platform(libs.kotlinx.coroutines.bom))
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.kotlin.testJunit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
}
