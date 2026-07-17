import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/** `com.android.library` + shared Android/Kotlin config. */
class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.library")
            extensions.configure<LibraryExtension> {
                configureAndroidCommon(this)
                defaultConfig {
                    minSdk = libs.intVersion("android-minSdk")
                }
                compileOptions {
                    sourceCompatibility = jdkJavaVersion
                    targetCompatibility = jdkJavaVersion
                }
            }
        }
    }
}
