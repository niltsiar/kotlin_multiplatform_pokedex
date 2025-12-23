plugins {
    id("convention.core.library")
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // Compose Unstyled
            implementation(libs.composeunstyled)
            implementation(libs.composeunstyled.theming)
            implementation(libs.composeunstyled.primitives)

            // Compose Multiplatform
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.ui)

            // Immutable Collections
            implementation(libs.kotlinx.collections.immutable)
        }
    }
}

android {
    namespace = "com.minddistrict.multiplatformpoc.core.designsystem.unstyled"
}
