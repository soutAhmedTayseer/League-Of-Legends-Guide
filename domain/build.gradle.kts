// :domain is pure Kotlin on purpose -- it must not be able to see Android at
// all. Phase 1 of the CMP/iOS migration (docs/plans/2026-08-29-compose-
// multiplatform-migration.md): kotlin-multiplatform replaces java-library.
// commonMain still can't see Android (AGENTS.md §3 is preserved by the
// module boundary, not by java-library specifically) and now also compiles
// for iOS. jvm() keeps the module consumable by :app/:data/:presentation
// exactly as before.
plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
    jvm {
        compilerOptions {
            jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11
        }
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.javax.inject)
            implementation(libs.kotlinx.coroutines.core)
        }
    }
}
