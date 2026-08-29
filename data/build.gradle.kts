plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.venom7t.lolguide.data"
    compileSdk {
        version = release(37) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        // GitLive's Firestore/Auth SDK ships reified inline functions
        // (get<T>()/set<T>()) compiled with JVM 17 bytecode; inlining those
        // into a JVM 11 target fails to compile. :app and :presentation
        // don't call those inline functions directly, so only :data needs
        // to move.
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation(project(":domain"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.javax.inject)

    implementation(platform(libs.koin.bom))
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.workmanager)

    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.work.runtime.ktx)

    implementation(libs.timber)

    // Phase 5: live service. :data holds the sync/auth repository impls,
    // same as every other *RepositoryImpl, so it needs the Firebase SDK
    // directly rather than routing calls through :app. GitLive's SDK
    // (Phase 3 of the CMP/iOS migration plan) wraps the native Android SDK
    // under the hood but exposes a suspend-native, KMP-portable API --
    // no more Task/.await() bridging needed. The native com.google.firebase
    // artifacts GitLive pulls in transitively declare no version of their
    // own -- the BoM is what resolves them, same as :app's direct deps.
    implementation(platform(libs.firebase.bom))
    implementation(libs.gitlive.firebase.auth)
    implementation(libs.gitlive.firebase.firestore)
}
