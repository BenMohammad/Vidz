package com.benmohammad.vidz.trimmer;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.SeekBar;

import androidx.annotation.NonNull;

public class VidzHgLVideoTrimmer extends FrameLayout {

    private static final String TAG = VidzHgLVideoTrimmer.class.getSimpleName();

    private static final int SHOW_PROGRESS_= 2;

    private SeekBar mHolderTopView;



    public VidzHgLVideoTrimmer(@NonNull Context context, AttributeSet attrs) {
        super(context, attrs, 0);
    }
}
