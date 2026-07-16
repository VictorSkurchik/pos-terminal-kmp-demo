import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

/**
 * Config shared by every Android module and reachable via [CommonExtension]: compileSdk from
 * the catalog and Kotlin's JVM target. Java compile options, `minSdk` and `targetSdk` live on
 * the concrete Library/Application extensions and are set by each convention plugin.
 */
internal fun Project.configureAndroidCommon(commonExtension: CommonExtension) {
    commonExtension.compileSdk = libs.intVersion("android-compileSdk")
    extensions.configure<KotlinAndroidProjectExtension> {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }
}
