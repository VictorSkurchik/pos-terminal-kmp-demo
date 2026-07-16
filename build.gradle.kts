plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.androidMultiplatformLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.ktor) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.detekt) apply false
}

// Point git at the versioned hooks (Conventional Commits guard). Run once after cloning.
tasks.register("installGitHooks") {
    group = "git"
    description = "Configure git to use the versioned .githooks directory."
    val hook = rootProject.file(".githooks/commit-msg")
    val repoRoot = rootProject.projectDir
    doLast {
        hook.setExecutable(true)
        ProcessBuilder("git", "config", "core.hooksPath", ".githooks")
            .directory(repoRoot)
            .inheritIO()
            .start()
            .waitFor()
        println("✓ git hooks installed (core.hooksPath = .githooks)")
    }
}