package com.benmohammad.vidz.interfaces

import java.io.File

interface VidzDialogHelper {

    fun setHelper(helper: VidzBaseCreatorDialogFragment.CallBacks)
    fun setMode(mode: Int)
    fun setFilePathFromSource(file: File)
    fun setDuration(duration: Long)


}
