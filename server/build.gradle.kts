plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.ktor)
    id("posterminal.quality")
}

group = "by.vsdev.posterminal.demo"
version = "1.0.0"
application {
    mainClass = "by.vsdev.posterminal.demo.ApplicationKt"
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    api(projects.core)
    implementation(libs.logback)
    implementation(libs.kotlinx.coroutines.core)

    implementation(libs.ktor.serverCore)
    implementation(libs.ktor.serverNetty)
    implementation(libs.ktor.serverContentNegotiation)
    implementation(libs.ktor.serializationJson)
    implementation(libs.ktor.serverCors)
    implementation(libs.ktor.serverStatusPages)
    implementation(libs.ktor.serverCallLogging)

    implementation(libs.room.runtime)
    implementation(libs.sqlite.bundled)
    ksp(libs.room.compiler)

    testImplementation(libs.ktor.serverTestHost)
    testImplementation(libs.kotlin.testJunit)
}
