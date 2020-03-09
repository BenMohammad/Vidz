package com.benmohammad.vidz.trimmer.interfaces;

import com.benmohammad.vidz.trimmer.view.VidzRangeSeekBarView;

public interface VidzOnRangeSeekBarListener {

    void onCreate(VidzRangeSeekBarView rangeSeekBarView, int index, float value);
    void onSeek(VidzRangeSeekBarView rangeSeekBarView, int index, float value);
    void onSeekStart(VidzRangeSeekBarView rangeSeekBarView, int index, float value);
    void onSeekStop(VidzRangeSeekBarView rangeSeekBarView, int index, float value);
}
