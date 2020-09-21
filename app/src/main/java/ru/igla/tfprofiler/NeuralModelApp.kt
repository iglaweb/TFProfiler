package ru.igla.tfprofiler

import android.annotation.SuppressLint
import android.app.Application
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import com.facebook.stetho.Stetho
import ru.igla.tfprofiler.core.Timber
import ru.igla.tfprofiler.utils.DevelopReportingTree

/**
 * Created by lashkov on 17/09/20.
 * Copyright (c) 2020 igla. All rights reserved.
 */
class NeuralModelApp : Application() {
    override fun onCreate() {
        super.onCreate()
        sInstance = this
        if (BuildConfig.DEBUG) {
            Timber.plant(DevelopReportingTree())
            Stetho.initializeWithDefaults(this)
        }
        if (BuildConfig.DEBUG && enableStrictMode) {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()
                    .penaltyLog()
                    .build()
            )
            StrictMode.setVmPolicy(
                VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .penaltyDeath()
                    .build()
            )
        }
    }

    companion object {
        private const val enableStrictMode = false

        @SuppressLint("StaticFieldLeak")
        private lateinit var sInstance: NeuralModelApp

        @get:Synchronized
        val instance: NeuralModelApp
            get() = sInstance
    }
}