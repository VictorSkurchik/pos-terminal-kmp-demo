// build-logic is a separate *included build* with its own settings, so the foojay resolver
// declared in the root settings does not apply here. Its convention project compiles on a
// JDK 17 toolchain (JavaLanguageVersion.of(17)); without this, Render's JDK-21-only image
// fails to configure :core with "Toolchain download repositories have not been configured".
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

rootProject.name = "build-logic"
include(":convention")
