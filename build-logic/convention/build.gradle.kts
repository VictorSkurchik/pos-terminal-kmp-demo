plugins {
    `kotlin-dsl`
}

group = "by.vsdev.posterminal.buildlogic"

// Convention plugins run on JDK 17 (same baseline as the modules they configure).
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.compose.gradlePlugin)
    compileOnly(libs.composeCompiler.gradlePlugin)
    compileOnly(libs.detekt.gradlePlugin)
    compileOnly(libs.ktlint.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "posterminal.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = "posterminal.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("androidCompose") {
            id = "posterminal.android.compose"
            implementationClass = "AndroidComposeConventionPlugin"
        }
        register("quality") {
            id = "posterminal.quality"
            implementationClass = "QualityConventionPlugin"
        }
    }
}
