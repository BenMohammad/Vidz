package com.benmohammad.vidz.trimmer.view;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.NonNull;

import com.benmohammad.vidz.R;

import java.util.List;
import java.util.Vector;

public class VidzThumb {

    public static final int LEFT = 0;
    public static final int RIGHT = 1;

    private int mIndex;
    private float mVal;
    private float mPos;
    private Bitmap mBitmap;
    private int mWidthBitmap;
    private int mHeightBitmap;

    private float mLastTouchX;

    public VidzThumb() {
        mVal = 0;
        mPos = 0;
    }

    int getIndex() {
        return mIndex;
    }

    private void setIndex(int index) {
        this.mIndex = index;
    }

    float getVal() {
        return mVal;
    }

    void setVal(float val) {
        this.mVal = val;
    }

    float getPos() {
        return mPos;
    }

    void setPos(float pos) {
        this.mPos = pos;
    }

    Bitmap getBitmap() {
        return mBitmap;
    }

    private void setBitmap(@NonNull Bitmap bitmap) {
        this.mBitmap = bitmap;
        mWidthBitmap = bitmap.getWidth();
        mHeightBitmap = bitmap.getHeight();
    }

    @NonNull
    static List<VidzThumb> initThumbs(Resources resources) {
        List<VidzThumb> thumbs = new Vector<>();

        for(int i = 0; i < 2; i++) {
            VidzThumb th = new VidzThumb();
            th.setIndex(i);
            if(i == 0) {
                int resImageLeft = R.drawable.apptheme_text_select_handle_left;
                th.setBitmap(BitmapFactory.decodeResource(resources, resImageLeft));
            } else {
                int resImageRight = R.drawable.apptheme_text_select_handle_right;
                th.setBitmap(BitmapFactory.decodeResource(resources, resImageRight));
            }

            thumbs.add(th);
        }
        return thumbs;
    }

    static int getWidthBitmap(@NonNull List<VidzThumb> thumbs) {
        return thumbs.get(0).mWidthBitmap;
    }

    static int getHeightBitmap(@NonNull List<VidzThumb> thumbs) {
        return thumbs.get(0).getHeightBitmap();
    }

    float getLastTouchX() {
        return mLastTouchX;
    }

    void setLastTouchX(float lastTouchX) {
        this.mLastTouchX = lastTouchX;
    }

    public int getWidthBitmap() {
        return mWidthBitmap;
    }

    public int getHeightBitmap() {
        return mHeightBitmap;
    }


}
