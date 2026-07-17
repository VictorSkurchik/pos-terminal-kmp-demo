rootProject.name = "pos-terminal"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

// Lets Gradle auto-provision the JDK 17 *compile* toolchain (jvmToolchain(17)) on machines
// that don't have it installed — e.g. the Render Docker image, which ships only JDK 21.
// Without this, `jvmToolchain(17)` fails with "Toolchain auto-provisioning is not enabled".
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

include(":app:androidApp")
include(":core")
include(":core:domain")
include(":core:ui")
include(":core:data")
include(":feature:pos")
include(":feature:mdm")
include(":feature:offer")
include(":server")