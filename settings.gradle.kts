pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

// Auto-provisions the Java 17 daemon toolchain declared in
// gradle/gradle-daemon-jvm.properties, so the build does not depend on
// whatever JDK happens to be on PATH.
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven(url = "https://jitpack.io")
    }
}

rootProject.name = "LOL Guide"
include(":app")
include(":domain")
include(":data")
include(":presentation")
include(":kmpSpike") // Phase 0 CMP/iOS migration spike -- throwaway, see docs/plans
