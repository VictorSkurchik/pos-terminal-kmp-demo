import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jlleitschuh.gradle.ktlint.KtlintExtension

/** ktlint (formatting) + detekt (static analysis), consistently configured across modules. */
class QualityConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("org.jlleitschuh.gradle.ktlint")
        pluginManager.apply("io.gitlab.arturbosch.detekt")

        extensions.configure<KtlintExtension> {
            android.set(true)
            ignoreFailures.set(false)
            // Don't lint generated code (Room/KSP output under build/).
            filter {
                exclude("**/build/**")
                exclude("**/generated/**")
            }
        }

        extensions.configure<DetektExtension> {
            buildUponDefaultConfig = true
            parallel = true
            val shared = rootProject.file("detekt.yml")
            if (shared.exists()) {
                config.setFrom(shared)
            }
        }
    }
}
