package com.example.lolguide

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class LolGuideApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Debug only: a release build should not be writing request URLs and
        // cache hits to logcat.
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
