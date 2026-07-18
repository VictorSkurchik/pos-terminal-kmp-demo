import java.util.Properties

plugins {
    id("posterminal.android.application")
    id("posterminal.android.compose")
    id("posterminal.quality")
    alias(libs.plugins.kotlinSerialization)
    // Parcelize ships with the built-in Kotlin plugin (AGP 9); apply it without a version.
    id("org.jetbrains.kotlin.plugin.parcelize")
}

// Semantic version — single source of truth in version.properties at the repo root.
val versionProps = Properties().apply {
    rootProject.file("version.properties").inputStream().use { load(it) }
}
val vMajor = versionProps.getProperty("MAJOR").trim().toInt()
val vMinor = versionProps.getProperty("MINOR").trim().toInt()
val vPatch = versionProps.getProperty("PATCH").trim().toInt()

// Optional release signing — configured only when keystore.properties is present (CI/local
// falls back to the debug key so unsigned builds never fail).
val keystoreProps = rootProject.file("keystore.properties")

dependencies {
    implementation(projects.core.domain)
    implementation(projects.core.ui)
    implementation(projects.feature.pos)
    implementation(projects.feature.mdm)
    implementation(projects.feature.offer)

    implementation(platform(libs.koin.bom))

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.compose.ui)
    implementation(libs.androidx.lifecycle.viewmodelCompose)
    implementation(libs.navigation.compose)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.koin.android)
    implementation(libs.koin.androidxCompose)
    implementation(libs.koin.androidxWorkmanager)
    implementation(libs.androidx.work.runtime)
    implementation(libs.androidx.datastore.preferences)
}

android {
    namespace = "by.vsdev.posterminal.demo"

    defaultConfig {
        applicationId = "by.vsdev.posterminal.demo"
        versionCode = vMajor * 10_000 + vMinor * 100 + vPatch
        versionName = "$vMajor.$vMinor.$vPatch"
    }

    buildFeatures {
        buildConfig = true
    }

    flavorDimensions += "env"
    productFlavors {
        create("dev") {
            dimension = "env"
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"
            // 10.0.2.2 = host loopback from the Android emulator.
            buildConfigField("String", "SERVER_URL", "\"http://10.0.2.2:8080\"")
        }
        create("staging") {
            dimension = "env"
            applicationIdSuffix = ".staging"
            versionNameSuffix = "-staging"
            buildConfigField("String", "SERVER_URL", "\"https://pos-terminal-kmp-demo-staging.onrender.com\"")
        }
        create("prod") {
            dimension = "env"
            buildConfigField("String", "SERVER_URL", "\"https://pos-terminal-kmp-demo.onrender.com\"")
        }
    }

    signingConfigs {
        if (keystoreProps.exists()) {
            create("release") {
                val props = Properties().apply { keystoreProps.inputStream().use { load(it) } }
                storeFile = rootProject.file(props.getProperty("storeFile"))
                storePassword = props.getProperty("storePassword")
                keyAlias = props.getProperty("keyAlias")
                keyPassword = props.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig = if (keystoreProps.exists()) {
                signingConfigs.getByName("release")
            } else {
                signingConfigs.getByName("debug")
            }
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    lint {
        // False positive: MainActivity extends androidx ComponentActivity (→ android.app.Activity),
        // but lint can't always resolve that hierarchy through the Compose Multiplatform artifacts.
        disable += "Instantiatable"
    }
}
