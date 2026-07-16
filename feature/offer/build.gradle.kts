plugins {
    id("posterminal.android.library")
    id("posterminal.android.compose")
    id("posterminal.quality")
}

android {
    namespace = "by.vsdev.posterminal.demo.feature.offer"
}

dependencies {
    implementation(projects.core.ui)
}
