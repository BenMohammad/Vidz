package com.benmohammad.vidz.trimmer;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;

import com.benmohammad.vidz.trimmer.view.VidzRangeSeekBarView;

public class VidzHgLVideoTrimmer extends FrameLayout {

    private static final String TAG = VidzHgLVideoTrimmer.class.getSimpleName();

    private static final int SHOW_PROGRESS_= 2;

    private SeekBar mHolderTopView;
    private VidzRangeSeekBarView mRangeSeekBarView;
    private RelativeLayout mlinearVideo;
    private View mTimeInfoContainer;
    private VideoView mVideoView;
    private ImageView mPlayView;
    private TextView mTextSize;
    private TextView mTextTimeFrame;
    private TextView mTextTime;




    public VidzHgLVideoTrimmer(@NonNull Context context, AttributeSet attrs) {
        super(context, attrs, 0);
    }
}
