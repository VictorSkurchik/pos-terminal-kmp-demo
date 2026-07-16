plugins {
    id("posterminal.android.library")
    alias(libs.plugins.ksp)
    id("posterminal.quality")
}

android {
    namespace = "by.vsdev.posterminal.demo.core.data"
}

dependencies {
    api(projects.core)
    api(projects.core.domain)

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.datastore.preferences)

    // Ktor client types are needed to map transport exceptions → DomainError in the data layer.
    implementation(libs.ktor.clientCore)
    implementation(libs.ktor.serializationJson)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.room.runtime)
    ksp(libs.room.compiler)

    implementation(libs.koin.android)

    testImplementation(libs.kotlin.testJunit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.ktor.clientMock)
    testImplementation(libs.ktor.clientContentNegotiation)
    testImplementation(libs.room.testing)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.testExt.junit)
}
