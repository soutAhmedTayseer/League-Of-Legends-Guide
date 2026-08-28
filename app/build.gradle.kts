import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
}

// The Riot API key is machine-local and must never be committed (AGENTS.md section 8.2).
// An absent or empty key is not an error: everything in Phases 0-3 is keyless,
// and keyed features are expected to degrade to a "not configured" state.
val riotApiKey: String = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) FileInputStream(file).use { load(it) }
}.getProperty("RIOT_API_KEY").orEmpty()

android {
    namespace = "com.example.lolguide"
    compileSdk {
        version = release(37) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.lolguide"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        buildConfigField("String", "RIOT_API_KEY", "\"$riotApiKey\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":data"))
    implementation(project(":presentation"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.splashscreen)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.navigation.compose)

    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    ksp(libs.hilt.compiler)

    // The Hilt modules in :app construct data-layer implementations, so :app
    // needs the libraries those constructors take as parameters.
    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    implementation(libs.androidx.datastore.preferences)

    implementation(libs.kotlinx.collections.immutable)
    implementation(libs.coil3.compose)
    implementation(libs.coil3.network)
    implementation(libs.timber)

    debugImplementation(libs.androidx.compose.ui.tooling)

}
