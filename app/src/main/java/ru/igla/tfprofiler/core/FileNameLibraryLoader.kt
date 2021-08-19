package ru.igla.tfprofiler.core

import ru.igla.tfprofiler.utils.logI
import timber.log.Timber

class FileNameLibraryLoader : LibraryLoader {
    override fun loadLibraryFile(filename: String) {
        try {
            System.loadLibrary(filename)
            logI { "Successfully loaded $filename" }
        } catch (e: UnsatisfiedLinkError) {
            Timber.e(e, " ### $filename library not found! ###")
        }
    }
}