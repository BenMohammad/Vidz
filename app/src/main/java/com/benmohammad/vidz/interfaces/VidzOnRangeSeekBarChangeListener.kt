package com.benmohammad.vidz.interfaces

import com.benmohammad.vidz.utils.VidzCustomRangeSeekBar

interface VidzOnRangeSeekBarChangeListener {

    fun onCreate(customRangeSeekBar: VidzCustomRangeSeekBar, index: Int, value: Float)
    fun onSeek(customRangeSeekBar: VidzCustomRangeSeekBar, index: Int, value: Float)
    fun onSeekStart(customRangeSeekBar: VidzCustomRangeSeekBar, index: Int, value: Float)
    fun onSeekStop(customRangeSeekBar: VidzCustomRangeSeekBar, index: Int, value: Float)
}