package com.thaicrew.splitup

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class SplitUpApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // A "árvore" de logs só é "plantada" em builds de depuração.
        // Em builds de release, as chamadas ao Timber não farão nada.
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}