package ru.igla.tfprofiler.core

class FileNameLibraryLoader : LibraryLoader {
    override fun loadLibraryFile(filename: String) {
        try {
            System.loadLibrary(filename)
            Timber.i("Successfully loaded $filename")
        } catch (e: UnsatisfiedLinkError) {
            Timber.e(e, " ### $filename library not found! ###")
        }
    }
}