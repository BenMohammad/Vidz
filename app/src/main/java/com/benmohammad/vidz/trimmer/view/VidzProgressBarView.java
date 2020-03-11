package com.benmohammad.vidz.trimmer.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.benmohammad.vidz.R;
import com.benmohammad.vidz.trimmer.interfaces.VidzOnProgressVideoListener;
import com.benmohammad.vidz.trimmer.interfaces.VidzOnRangeSeekBarListener;

public class VidzProgressBarView extends View implements VidzOnRangeSeekBarListener, VidzOnProgressVideoListener {

    private int mProgressHeight;
    private int mViewWidth;

    private final Paint mBackgroundColor = new Paint();
    private final Paint mProgressColor = new Paint();

    private Rect mBackgroundRect;
    private Rect mProgressRect;



    public VidzProgressBarView(@NonNull Context context, AttributeSet attr) {
        this(context, attr, 0);
    }

    public VidzProgressBarView(@NonNull Context context, AttributeSet attr, int defStyleAttr) {
        super(context, attr, defStyleAttr);
        init();
    }

    private void init() {
        int lineProgress = ContextCompat.getColor(getContext(), R.color.line_color);
        int lineBackground = ContextCompat.getColor(getContext(), R.color.line_color);

        mProgressHeight = getContext().getResources().getDimensionPixelOffset(R.dimen.progress_video_line_height);

        mBackgroundColor.setAntiAlias(true);
        mBackgroundColor.setColor(lineBackground);

        mProgressColor.setAntiAlias(true);
        mProgressColor.setColor(lineProgress);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int minW = getPaddingLeft() + getPaddingRight() + getSuggestedMinimumWidth();
        mViewWidth = resolveSizeAndState(minW, widthMeasureSpec, 1);

        int minH = getPaddingBottom() + getPaddingTop() + getMinimumHeight();
        int viewHeight = resolveSizeAndState(minH, heightMeasureSpec, 1);

        setMeasuredDimension(mViewWidth, viewHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawBackground(canvas);
        drawLineProgress(canvas);

    }

    private void drawBackground(@NonNull Canvas canvas) {
        if(mBackgroundRect != null) {
            canvas.drawRect(mBackgroundRect, mBackgroundColor);
        }
    }

    private void drawLineProgress(@NonNull Canvas canvas) {
        if(mProgressRect != null) {
            canvas.drawRect(mProgressRect, mProgressColor);
        }
    }



    @Override
    public void onCreate(VidzRangeSeekBarView rangeSeekBarView, int index, float value) {
        updateBackgroundRect(index, value);
    }

    @Override
    public void onSeek(VidzRangeSeekBarView rangeSeekBarView, int index, float value) {
        updateBackgroundRect(index, value);
    }

    @Override
    public void onSeekStart(VidzRangeSeekBarView rangeSeekBarView, int index, float value) {
        updateBackgroundRect(index, value);
    }

    @Override
    public void onSeekStop(VidzRangeSeekBarView rangeSeekBarView, int index, float value) {
        updateBackgroundRect(index, value);
    }

    private void updateBackgroundRect(int index, float value) {
        if(mBackgroundRect == null) {
            mBackgroundRect = new Rect(0, 0, mViewWidth, mProgressHeight);
        }

        int newValue = (int) ((mViewWidth * value) / 100);
        if(index == 0) {
            mBackgroundRect = new Rect(newValue, mBackgroundRect.top, mBackgroundRect.right, mBackgroundRect.bottom);
        } else {
            mBackgroundRect = new Rect(mBackgroundRect.left, mBackgroundRect.top, newValue, mBackgroundRect.bottom);
        }

        updateProgress(0, 0, 0.0f);
    }

    @Override
    public void updateProgress(int tine, int max, float scale) {
        if(scale == 0) {
            mProgressRect = new Rect(0, mBackgroundRect.top, 0, mBackgroundRect.bottom);
        } else {
            int newValue = (int) ((mViewWidth * scale) / 100);
            mProgressRect = new Rect(mBackgroundRect.left,mBackgroundRect.top, newValue, mBackgroundRect.bottom);
        }
        invalidate();
    }
}
