package com.benmohammad.vidz.trimmer.view;

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
import com.benmohammad.vidz.trimmer.interfaces.VidzOnRangeSeekBarListener;

import java.util.ArrayList;
import java.util.List;

public class VidzRangeSeekBarView extends View {

    private int mHeightTimeLIne;
    private List<VidzThumb> mThumbs;
    private List<VidzOnRangeSeekBarListener> mListeners;
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

    public VidzRangeSeekBarView(@NonNull Context context, AttributeSet attr) {
        this(context, attr, 0);
    }

    public VidzRangeSeekBarView(@NonNull Context context, AttributeSet attr, int defStyleAttr) {
        super(context, attr, defStyleAttr);
        init();
    }

    private void init() {
        mThumbs =  VidzThumb.initThumbs(getResources());
        mThumbWidth  = VidzThumb.getWidthBitmap(mThumbs);
        mThumbHeight = VidzThumb.getHeightBitmap(mThumbs);

        mScaleRangeMax = 100;
        mHeightTimeLIne = getContext().getResources().getDimensionPixelOffset(R.dimen.frames_video_height);

        setFocusable(true);
        setFocusableInTouchMode(true);

        mFirstRun = true;

        int shadowColor = ContextCompat.getColor(getContext(), R.color.shadow_color);
        mShadow.setAntiAlias(true);
        mShadow.setColor(shadowColor);
        mShadow.setAlpha(117);

        int lineColor = ContextCompat.getColor(getContext(), R.color.line_color);
        mLine.setAntiAlias(true);
        mLine.setColor(lineColor);

    }

    public void initMaxWidth() {
        mMaxWidth = mThumbs.get(1).getPos() - mThumbs.get(0).getPos();

        onSeekStop(this, 0,mThumbs.get(0).getVal());
        onSeekStop(this, 1,mThumbs.get(1).getVal());


    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int minW = getPaddingLeft() + getPaddingRight() + getSuggestedMinimumWidth();
        mViewWidth = resolveSizeAndState(minW, widthMeasureSpec, 1);

        int minH = getPaddingBottom() + getPaddingTop() + getSuggestedMinimumHeight();
        int viewHeight = resolveSizeAndState(minH, heightMeasureSpec, 1);

        setMeasuredDimension(mViewWidth, viewHeight);

        mPixelRangeMin = 0;
        mPixelRangeMax = mViewWidth - mThumbWidth;

        if(mFirstRun) {
            for(int i = 0; i < mThumbs.size(); i++) {
                VidzThumb th = mThumbs.get(i);
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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final VidzThumb mThumb;
        final VidzThumb mThumb2;
        final float coordinate = event.getX();
        final int action = event.getAction();

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                currentThumb = getClosestThumb(coordinate);

                if(currentThumb == -1) {
                    return false;
                }

                mThumb = mThumbs.get(currentThumb);
                mThumb.setLastTouchX(coordinate);
                onSeekStart(this, currentThumb, mThumb.getVal());
                return true;
            }
            case MotionEvent.ACTION_UP: {
                if(currentThumb == -1) {
                    return false;
                }

                mThumb = mThumbs.get(currentThumb);
                onSeekStop(this, currentThumb, mThumb.getVal());
                return true;
            }

            case MotionEvent.ACTION_MOVE: {
                mThumb = mThumbs.get(currentThumb);
                mThumb2 = mThumbs.get(currentThumb == 0? 1: 0);

                final float dx = coordinate - mThumb.getLastTouchX();
                final float newX = mThumb.getPos() + dx;

                if(currentThumb == 0) {
                    if((newX + mThumb.getWidthBitmap()) == mThumb2.getPos()) {
                        mThumb.setPos(mThumb2.getPos() - mThumb.getWidthBitmap());
                    } else if(newX <= mPixelRangeMin) {
                        mThumb.setPos(mPixelRangeMin);
                    } else {
                        checkPositionThumb(mThumb, mThumb2, dx, true);
                        mThumb.setPos(mThumb.getPos() + dx);
                        mThumb.setLastTouchX(coordinate);
                    }
                } else {
                    if(newX <= mThumb2.getPos() + mThumb2.getWidthBitmap()){
                        mThumb.setPos(mThumb2.getPos() + mThumb.getWidthBitmap());
                    } else if(newX >= mPixelRangeMax) {
                        mThumb.setPos(mPixelRangeMax);
                    } else {
                        checkPositionThumb(mThumb2, mThumb, dx, false);

                        mThumb.setPos(mThumb.getPos() + dx);
                        mThumb.setLastTouchX(coordinate);
                    }
                }

                setThumbPos(currentThumb, mThumb.getPos());

                invalidate();
                return true;
            }
        }
        return false;
    }

    private void checkPositionThumb(@NonNull VidzThumb mThumbLeft, @NonNull VidzThumb mThumbRight, float dx, boolean isLeftMove) {
        if(isLeftMove && dx < 0) {
            if((mThumbRight.getPos() - (mThumbLeft.getPos() + dx)) > mMaxWidth) {
                mThumbRight.setPos(mThumbLeft.getPos() + dx + mMaxWidth);
                setThumbPos(1, mThumbRight.getPos());
            }
        } else if(!isLeftMove && dx > 0) {
                if(((mThumbRight.getPos() + dx) - mThumbLeft.getPos()) > mMaxWidth) {
                    mThumbLeft.setPos(mThumbRight.getPos() + dx + mMaxWidth);
                    setThumbPos(0, mThumbLeft.getPos());
                }
        }
    }


    private int currentThumb = 0;

    private void onCreate(VidzRangeSeekBarView rangeSeekBarView, int index, float value) {
        if(mListeners == null) {
            return;
        }

        for(VidzOnRangeSeekBarListener item: mListeners) {
            item.onCreate(rangeSeekBarView, index, value);
        }
    }

    private void onSeek(VidzRangeSeekBarView rangeSeekBarView, int index, float value) {
        if(mListeners == null) {
            return;
        }

        for(VidzOnRangeSeekBarListener item : mListeners) {
            item.onSeek(rangeSeekBarView, index, value);
        }
    }

    private void onSeekStart(VidzRangeSeekBarView rangeSeekBarView, int index, float value) {
        if(mListeners == null) {
            return;
        }

        for(VidzOnRangeSeekBarListener item : mListeners) {
            item.onSeekStart(rangeSeekBarView, index, value);
        }
    }

    private void onSeekStop(VidzRangeSeekBarView rangeSeekBarView, int index, float value) {
        if(mListeners == null) {
            return;
        }

        for(VidzOnRangeSeekBarListener item : mListeners) {
            item.onSeekStop(rangeSeekBarView, index, value);
        }
    }

    public List<VidzThumb> getThumbs() {
        return mThumbs;
    }

    private float pixelToScale(int index, float pixelValue) {
        float scale = (pixelValue * 100) / mPixelRangeMax;
        if(index == 0) {
            float pxThumb = (scale * mThumbWidth) / 100;
            return scale - (pxThumb * 100) /mPixelRangeMax;
        } else {
            float pxThumb = ((100 * scale) * mThumbWidth) / 100;
            return scale - (pxThumb * 100) / mPixelRangeMax;
        }
    }

    private float scaleToPixel(int index, float scaleValue) {
        float px = (scaleValue * mPixelRangeMax) / 100;
        if(index == 0) {
            float pxThumb = (scaleValue * mThumbWidth) / 100;
            return px - pxThumb;
        } else {
            float pxThumb = ((100 - scaleValue) * mThumbWidth) / 100;
            return px + pxThumb;
        }
    }


    private void calculateThumbValue(int index) {
        if(index < mThumbs.size() && !mThumbs.isEmpty()) {
            VidzThumb th = mThumbs.get(index);
            th.setVal(pixelToScale(index, th.getPos()));
            onSeek(this, index, th.getVal());
        }
    }

    private void calculateThumbPos(int index) {
        if(index < mThumbs.size() && !mThumbs.isEmpty()) {
            VidzThumb th = mThumbs.get(index);
            th.setPos(scaleToPixel(index, th.getVal()));

        }
    }

    private float getThumbValue(int index) {
        return mThumbs.get(index).getVal();
    }

    public void setThumbValue(int index, float value) {
            mThumbs.get(index).setVal(value);
            calculateThumbPos(index);
            invalidate();
    }

    private void setThumbPos(int index, float pos) {
        mThumbs.get(index).setPos(pos);
        calculateThumbValue(index);
        invalidate();
    }

    private int getClosestThumb(float coordinate) {
        int closest = -1;
        if(!mThumbs.isEmpty()) {
            for(int i = 0; i < mThumbs.size(); i++) {
                final float tcoordinate = mThumbs.get(i).getPos() + mThumbWidth;
                if(coordinate >= mThumbs.get(i).getPos() && coordinate <= tcoordinate) {
                    closest = mThumbs.get(i).getIndex();
                }
            }
        }

        return closest;
    }

    private void drawShadow(@NonNull Canvas canvas) {
        if(!mThumbs.isEmpty()) {
            for(VidzThumb th : mThumbs) {
                if(th.getIndex() == 0) {
                    final float x = th.getPos() + getPaddingLeft();
                    if(x > mPixelRangeMin) {
                        Rect mRect = new Rect((int) mThumbWidth, 0, (int) (x + mThumbWidth), mHeightTimeLIne);
                        canvas.drawRect(mRect, mShadow);
                    }
                } else {
                    final float x = th.getPos() - getPaddingRight();
                    if(x < mPixelRangeMax) {
                        Rect mRect = new Rect((int) x, 0, (int)(mViewWidth - mThumbWidth), mHeightTimeLIne);
                        canvas.drawRect(mRect, mShadow);
                    }
                }
            }
        }
    }

    private void drawThumbs(@NonNull Canvas canvas) {
        if(!mThumbs.isEmpty()) {
            for(VidzThumb th : mThumbs) {
                if(th.getIndex() == 0) {
                    canvas.drawBitmap(th.getBitmap(), th.getPos() + getPaddingLeft(), getPaddingTop() + mHeightTimeLIne, null);
                } else {
                    canvas.drawBitmap(th.getBitmap(), th.getPos() - getPaddingRight(), getPaddingTop() + mHeightTimeLIne, null);
                }
            }
        }
    }

    public void addOnSeekBarListener(VidzOnRangeSeekBarListener listener) {
        if(mListeners == null) {
            mListeners = new ArrayList<>();
        }
        mListeners.add(listener);
    }

}
