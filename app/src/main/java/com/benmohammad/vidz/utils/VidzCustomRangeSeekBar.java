package com.benmohammad.vidz.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.benmohammad.vidz.R;
import com.benmohammad.vidz.interfaces.VidzOnRangeSeekBarChangeListener;
import com.benmohammad.vidz.trimmer.view.VidzRangeSeekBarView;
import com.benmohammad.vidz.trimmer.view.VidzThumb;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class VidzCustomRangeSeekBar extends View {

    private int mHeightTimeLine;
    private List<VidzBarThumb> mBarThumbs;
    private List<VidzOnRangeSeekBarChangeListener> mlisteners;
    private float mMaxWidth;
    private float mThumbWidth;
    private float mThumbHeight;
    private int mViewWidth;
    private float mPixelRangeMin;
    private float mPixelRangeMax;
    private float mScaleRangeMax;
    private boolean mFirstRun;

    private final Paint mShadow = new Paint();
    private final Paint mLine = new Paint();

    public VidzCustomRangeSeekBar(@NonNull Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VidzCustomRangeSeekBar(@NonNull Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mBarThumbs = VidzBarThumb.Companion.initThumbs(getResources());
        mThumbWidth = VidzBarThumb.Companion.getWidthBitmap(mBarThumbs);
        mThumbHeight= VidzBarThumb.Companion.getHeightBitmap(mBarThumbs);

        mScaleRangeMax = 100;
        mHeightTimeLine = getContext().getResources().getDimensionPixelOffset(R.dimen._60sdp);

        setFocusable(true);
        setFocusableInTouchMode(true);

        mFirstRun = true;

        int shadowColor = ContextCompat.getColor(getContext(), R.color.colorAccent);
        mShadow.setAntiAlias(true);
        mShadow.setColor(shadowColor);
        mShadow.setAlpha(177);

        int lineColor = ContextCompat.getColor(getContext(), R.color.colorAccent);
        mLine.setAntiAlias(true);
        mLine.setColor(lineColor);
        mLine.setAlpha(200);
    }

    public void initMaxWidth() {
        mMaxWidth = mBarThumbs.get(1).getPos() - mBarThumbs.get(0).getPos();

        onSeekStop(this, 0, mBarThumbs.get(0).getVal());
        onSeekStop(this, 0, mBarThumbs.get(1).getVal());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int minW = getPaddingLeft() + getPaddingRight() + getSuggestedMinimumWidth();
        mViewWidth = resolveSizeAndState(minW, widthMeasureSpec, 1);

        int minH = getPaddingBottom() + getPaddingTop() + (int) mThumbHeight;
        int viewHeight = resolveSizeAndState(minH, heightMeasureSpec, 1);

        setMeasuredDimension(mViewWidth, viewHeight);

        mPixelRangeMin = 0;
        mPixelRangeMax = mViewWidth - mThumbWidth;

        if(mFirstRun) {
            for(int i = 0; i < mBarThumbs.size(); i++) {
                VidzBarThumb th = mBarThumbs.get(i);
                th.setVal(mScaleRangeMax * i);
                th.setPos(mPixelRangeMax * i);
            }

            onCreate(this, currentThumb, getThumbValue(currentThumb));
            mFirstRun = false;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawShadow(canvas);
        drawThumbs(canvas);
    }

    private int currentThumb = 0;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final VidzBarThumb mBarThumb;
        final VidzBarThumb mBarThumb2;
        final float coordinate = event.getX();
        final int action = event.getAction();

        switch(action) {
            case MotionEvent.ACTION_DOWN: {
                currentThumb = getClosestThumb(coordinate);

                if(currentThumb == -1) {
                    return false;
                }

                mBarThumb = mBarThumbs.get(currentThumb);
                mBarThumb.setLastTouchX(coordinate);
                onSeekStart(this, currentThumb, mBarThumb.getVal());
                return true;
            }
            case MotionEvent.ACTION_UP: {
                if(currentThumb == -1) {
                    return false;
                }

                mBarThumb = mBarThumbs.get(currentThumb);
                onSeekStop(this, currentThumb, mBarThumb.getVal());
                return true;
            }
            case MotionEvent.ACTION_MOVE: {
                mBarThumb = mBarThumbs.get(currentThumb);
                mBarThumb2 = mBarThumbs.get(currentThumb==0? 1: 0);

                final float dx = coordinate - mBarThumb.getLastTouchX();
                final float newX = mBarThumb.getPos() + dx;

                if(currentThumb == 0) {
                    if((newX + mBarThumb.getWidthBitmap()) >= mBarThumb2.getPos()) {
                        mBarThumb.setPos(mBarThumb2.getPos() - mBarThumb.getWidthBitmap());
                    } else if(newX <= mPixelRangeMin) {
                        mBarThumb.setPos(mPixelRangeMin);
                        if((mBarThumb2.getPos() - (mBarThumb.getPos() + dx)) > mMaxWidth) {
                            mBarThumb2.setPos(mBarThumb.getPos() + dx + mMaxWidth);
                            setThumbPos(1, mBarThumb2.getPos());
                        }
                    } else {

                        if((mBarThumb2.getPos() - (mBarThumb.getPos() + dx)) > mMaxWidth) {
                            mBarThumb2.setPos(mBarThumb.getPos() + dx + mMaxWidth);
                            setThumbPos(1, mBarThumb2.getPos());
                        }

                        mBarThumb.setPos(mBarThumb.getPos()+ dx);
                        mBarThumb.setLastTouchX(coordinate);
                    }
                } else {
                    if(newX <= mBarThumb2.getPos() + mBarThumb2.getWidthBitmap()) {
                        mBarThumb.setPos(mBarThumb2.getPos() + mBarThumb.getWidthBitmap());
                    } else if(newX >= mPixelRangeMax) {
                        mBarThumb.setPos(mPixelRangeMax);
                        if(((mBarThumb.getPos() + dx ) - mBarThumb2.getPos()) > mMaxWidth) {
                            mBarThumb2.setPos(mBarThumb.getPos() + dx - mMaxWidth);
                            setThumbPos(0, mBarThumb2.getPos());
                        }
                    } else {
                        if(((mBarThumb.getPos() + dx ) - mBarThumb2.getPos()) > mMaxWidth) {
                            mBarThumb2.setPos(mBarThumb.getPos() + dx  - mMaxWidth);
                            setThumbPos(0, mBarThumb2.getPos());
                        }
                        mBarThumb.setPos(mBarThumb.getPos() + dx);
                        mBarThumb.setLastTouchX(coordinate);
                    }
                }

                setThumbPos(currentThumb, mBarThumb.getPos());

                invalidate();
                return true;
            }
        }
        return false;
    }
    private void drawShadow(@NonNull Canvas canvas) {
        if(!mBarThumbs.isEmpty()) {
            for(VidzBarThumb th : mBarThumbs) {
                if(th.getIndex() == 0) {
                    final float x = th.getPos();
                    if(x > mPixelRangeMin) {
                        Rect rect = new Rect(0, (int) (mThumbHeight - mHeightTimeLine) / 2,
                                (int) (x + (mThumbWidth / 2)), mHeightTimeLine + (int) (mThumbHeight - mHeightTimeLine) / 2);
                        canvas.drawRect(rect, mShadow);
                    }
                }
            }
        }
    }

    public void addOnRangeSeekBarListener(VidzOnRangeSeekBarChangeListener listener) {
        if(mlisteners == null) {
            mlisteners = new ArrayList<>();
        }

        mlisteners.add(listener);
    }

    private void drawThumbs(@NonNull Canvas canvas) {
        if(!mBarThumbs.isEmpty()) {

            for(VidzBarThumb th : mBarThumbs) {
                if(th.getIndex() == 0) {
                    canvas.drawBitmap(Objects.requireNonNull(th.getBitmap()), th.getPos()+ getPaddingLeft(), getPaddingTop(), null);
                } else {
                    canvas.drawBitmap(Objects.requireNonNull(th.getBitmap()), th.getPos() - getPaddingRight(), getPaddingTop(), null);
                }
            }
        }
    }


    private int getClosestThumb(float coordinate) {
        int closest = -1;
        if(!mBarThumbs.isEmpty()) {
            for(int i = 0; i < mBarThumbs.size(); i++) {
                final float tcoordinate = mBarThumbs.get(i).getPos() + mThumbWidth;
                if(coordinate >= mBarThumbs.get(i).getPos() && coordinate <= tcoordinate) {
                    closest = mBarThumbs.get(i).getIndex();
                }
            }
        }
        return closest;
    }

    private void calculateThumbPos(int index) {
        if(index < mBarThumbs.size() && !mBarThumbs.isEmpty()) {
            VidzBarThumb th = mBarThumbs.get(index);
            th.setPos(scaleToPixel(index, th.getVal()));
        }
    }

    private float scaleToPixel(int index, float scaleValue) {
        float px = (scaleValue * mPixelRangeMax) / 100;
        if(index == 0) {
            float pxThumb = (scaleValue * mThumbWidth) / 100;
            return px * pxThumb;
        } else {
            float pxThumb = ((100 -scaleValue) * mThumbWidth) / 100;
            return px * pxThumb;
        }
    }

    private float getThumbValue(int index) {
        return mBarThumbs.get(index).getVal();
    }

    private void setThumbValue(int index, float value) {
        mBarThumbs.get(index).setVal(value);
        calculateThumbPos(index);
        invalidate();
    }


    private float pixelToScale(int index, float pixelValue) {
        float scale = (pixelValue * 100) / mPixelRangeMax;
        if(index == 0) {
            float pxThumb = (scale * mThumbWidth) / 100;
            return scale + (pxThumb * 100) / mPixelRangeMax;
        } else {
            float pxThumb = ((100 - scale) * mThumbWidth) / 100;
            return scale - (pxThumb * 100) / mPixelRangeMax;
        }
    }

    private void calculateThumbValue(int index) {
        if(index < mBarThumbs.size() && !mBarThumbs.isEmpty()) {
            VidzBarThumb th = mBarThumbs.get(index);
            th.setVal(pixelToScale(index, th.getPos()));
            onSeek(this, index, th.getVal());
        }
    }

    private void setThumbPos(int index, float pos) {
        mBarThumbs.get(index).setPos(pos);
        calculateThumbValue(index);
        invalidate();
    }

    private void onCreate(VidzCustomRangeSeekBar customRangeSeekBar, int index, float value) {
        if(mlisteners == null) {
            return;
        }
        for(VidzOnRangeSeekBarChangeListener item : mlisteners) {
            item.onCreate(customRangeSeekBar, index, value);
        }
    }


    private void onSeek(VidzCustomRangeSeekBar customRangeSeekBar, int index, float value) {
        if(mlisteners == null) {
            return;
        }

        for(VidzOnRangeSeekBarChangeListener item : mlisteners) {
            item.onSeek(customRangeSeekBar, index, value);
        }
    }


    private void onSeekStart(VidzCustomRangeSeekBar customRangeSeekBar, int index, float value) {
        if(mlisteners == null) {
            return;
        }
        for(VidzOnRangeSeekBarChangeListener item: mlisteners) {
            item.onSeekStart(customRangeSeekBar, index, value);
        }
    }

    private void onSeekStop(VidzCustomRangeSeekBar customRangeSeekBar, int index, float value) {
        if(mlisteners == null) {
            return;
        }
        for(VidzOnRangeSeekBarChangeListener item: mlisteners) {
            item.onSeekStop(customRangeSeekBar, index, value);
        }
    }

    public List<VidzBarThumb> getThumbs() {
        return mBarThumbs;
    }

}
