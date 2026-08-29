// :domain is pure Kotlin on purpose -- it must not be able to see Android at
// all. Phase 1 of the CMP/iOS migration (docs/plans/2026-08-29-compose-
// multiplatform-migration.md): kotlin-multiplatform replaces java-library.
// commonMain still can't see Android (AGENTS.md §3 is preserved by the
// module boundary, not by java-library specifically) and now also compiles
// for iOS. jvm() keeps the module consumable by :app/:data/:presentation
// exactly as before.
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.ksp)
}

kotlin {
    jvm {
        compilerOptions {
            jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11
        }
        compilations.all {
            compileJavaTaskProvider?.configure {
                sourceCompatibility = "11"
                targetCompatibility = "11"
            }
        }
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.koin.core)
            implementation(libs.koin.annotations)
        }
    }
}

// Phase 2 (Hilt -> Koin): Koin Annotations' KSP processor scans this
// module's classes for @Factory/@Single and generates a Koin module --
// the KMP equivalent of what Hilt's codegen did automatically for any
// @Inject-constructor class. Only wired for the jvm target for now: the iOS
// targets are disabled on this (Windows) machine anyway (see
// kmpSpike/PHASE0_FINDINGS.md), and the generated module is only consumed
// by the Android app today.
dependencies {
    add("kspJvm", libs.koin.ksp.compiler)
}
