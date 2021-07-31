package ru.igla.tfprofiler

import android.annotation.SuppressLint
import android.app.Application
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import com.facebook.stetho.Stetho
import ru.igla.tfprofiler.analytics.InitAnalyticsImpl
import ru.igla.tfprofiler.core.FileNameLibraryLoader
import ru.igla.tfprofiler.core.Timber
import ru.igla.tfprofiler.utils.DevelopReportingTree

/**
 * Created by lashkov on 17/09/20.
 * Copyright (c) 2020 igla. All rights reserved.
 */
class TFProfilerApp : Application() {

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
        initTrackers()
    }

    private fun initTrackers() {
        val initAnalytics = InitAnalyticsImpl()
        initAnalytics.init(this)
    }

    companion object {
        private const val enableStrictMode = false

        @SuppressLint("StaticFieldLeak")
        private lateinit var sInstance: TFProfilerApp

        @get:Synchronized
        val instance: TFProfilerApp
            get() = sInstance

        init {
            val fileNameLibraryLoader = FileNameLibraryLoader()
            fileNameLibraryLoader.loadLibraryFile("opencv_java4")
        }
    }
}