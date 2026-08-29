// :presentation reshaped into a Kotlin Multiplatform module (Phase 4 of the
// CMP/iOS migration plan) -- Android target only exercised for now, exactly
// like :domain's Phase 1 move: all existing Jetpack Compose UI code moved
// into androidMain verbatim, unchanged. It does not run on iOS yet -- every
// screen still uses androidx.compose.* (Android-only) rather than Compose
// Multiplatform's org.jetbrains.compose.* artifacts; that swap, plus the
// 324-call-site resource migration, is the rest of Phase 4, not done here.
// This step only gets the module's *shape* ready and clears its Koin
// Annotations/KSP dependency first, since KSP does not support
// com.android.kotlin.multiplatform.library yet (google/ksp#2476, open) --
// see PresentationModule.kt for the hand-written Koin module this now needs.
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    @Suppress("DEPRECATION")
    android {
        namespace = "com.venom7t.lolguide.presentation"
        compileSdk = 37
        minSdk = 24

        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        }

        androidResources {
            enable = true
        }
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        androidMain.dependencies {
            implementation(project(":domain"))

            implementation(libs.androidx.core.ktx)
            implementation(libs.androidx.appcompat)
            implementation(libs.androidx.lifecycle.runtime.ktx)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.androidx.lifecycle.viewmodel.compose)

            implementation(libs.androidx.compose.ui)
            implementation(libs.androidx.compose.ui.graphics)
            implementation(libs.androidx.compose.ui.tooling.preview)
            implementation(libs.androidx.compose.material3)
            implementation(libs.androidx.compose.material.icons.extended)
            implementation(libs.androidx.compose.foundation)
            implementation(libs.androidx.navigation.compose)

            implementation(libs.koin.core)
            implementation(libs.koin.android)
            implementation(libs.koin.androidx.compose)

            implementation(libs.kotlinx.collections.immutable)
            implementation(libs.coil3.compose)
            implementation(libs.coil3.network)
            implementation(libs.media3.exoplayer)
            implementation(libs.media3.ui)
            implementation(libs.androidx.palette)
            implementation(libs.timber)

            // Phase 5 addendum: Google sign-in. Credential Manager's UI flow
            // is triggered from a Composable (needs a Context/Activity), so
            // the call itself lives here; only the resulting ID token
            // crosses into :domain.
            implementation(libs.androidx.credentials)
            implementation(libs.androidx.credentials.play.services.auth)
            implementation(libs.googleid)

            // A KMP android-library target publishes one variant, not a
            // debug/release split, so there is no debugImplementation
            // configuration to put this behind the way the classic
            // android-library setup did.
            implementation(libs.androidx.compose.ui.tooling)
        }
    }
}

dependencies {
    add("androidMainImplementation", platform(libs.androidx.compose.bom))
    add("androidMainImplementation", platform(libs.koin.bom))
}
