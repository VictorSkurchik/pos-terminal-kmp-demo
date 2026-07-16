import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.kotlinSerialization)
    id("posterminal.quality")
}

kotlin {
    jvmToolchain(17)

    jvm()

    js {
        browser()
    }

    androidLibrary {
        namespace = "by.vsdev.posterminal.demo.core"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget = JvmTarget.JVM_17
        }
        withHostTest {
            isIncludeAndroidResources = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.ktor.clientCore)
            implementation(libs.ktor.clientContentNegotiation)
            implementation(libs.ktor.serializationJson)
            implementation(libs.ktor.clientLogging)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }

        val jvmMain by getting {
            dependencies {
                implementation(libs.ktor.clientOkhttp)
            }
        }
        androidMain.dependencies {
            implementation(libs.ktor.clientOkhttp)
        }
        jsMain.dependencies {
            implementation(libs.ktor.clientJs)
        }
    }
}
