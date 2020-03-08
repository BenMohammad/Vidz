package com.benmohammad.vidz.trimmer.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;

public class VidzRangeSeekBarView extends View {

    private int mHeightTimeLIne;


    public VidzRangeSeekBarView(@NonNull Context context, AttributeSet attr) {
        this(context, attr, 0);
    }

    public VidzRangeSeekBarView(@NonNull Context context, AttributeSet attr, int defStyleAttr) {
        super(context, attr, defStyleAttr);
    }
}
