import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Enables Compose Multiplatform + the Compose compiler and wires the tooling/preview
 * dependencies that every Compose module otherwise repeats.
 */
class AndroidComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.compose")
            pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

            dependencies.add("implementation", libs.findLibrary("compose-uiToolingPreview").get())
            dependencies.add("debugImplementation", libs.findLibrary("compose-uiTooling").get())
        }
    }
}
