// Phase 0 spike ONLY -- throwaway module to prove the KMP + iOS toolchain
// cooperates with this project's Gradle/AGP/Kotlin versions. Not wired into
// :app, :domain, :data, or :presentation. Safe to delete wholesale.
plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
    jvm()

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
        }
    }
}
