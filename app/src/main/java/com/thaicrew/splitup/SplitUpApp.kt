package com.thaicrew.splitup

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class SplitUpApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // A arvore de logs so e plantada em builds de depuracao.
        // Em builds de release, as chamadas ao Timber nao farao nada.
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
            runDebugSeeder()
        }
    }

    private fun runDebugSeeder() {
        runCatching {
            val seederClass = Class.forName("com.thaicrew.splitup.debug.DebugDatabaseSeeder")
            val method = seederClass.getDeclaredMethod("seedIfNeeded", Application::class.java)
            method.invoke(null, this)
        }.onFailure { throwable ->
            Timber.w(throwable, "DebugDatabaseSeeder indisponivel.")
        }
    }
}