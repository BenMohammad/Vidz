package com.benmohammad.vidz.interfaces

import java.io.File

interface VidzFFMpegCallback {

    fun onProgress(progress: String)
    fun onSuccess(convertedFile: File, type: String)
    fun onFailure(error: Exception)
    fun onNotAvailable(error: Exception)
    fun onFinish()
}