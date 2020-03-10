package com.benmohammad.vidz.trimmer.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.media.MediaMetadata;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.LongSparseArray;
import android.view.View;

import androidx.annotation.NonNull;

import com.benmohammad.vidz.R;
import com.benmohammad.vidz.trimmer.utils.VidzBackgroundExecutor;
import com.benmohammad.vidz.trimmer.utils.VidzUiThreadExecutor;

public class VidzTimeLineView extends View {

    private Uri mVideoUri;
    private int mHeightView;
    private LongSparseArray<Bitmap> mBitmapList = null;

    public VidzTimeLineView(@NonNull Context context, AttributeSet attr) {
        this(context, attr, 0);
    }

    public VidzTimeLineView(@NonNull Context context, AttributeSet attr, int defStyleAttr) {
        super(context, attr, defStyleAttr);
        init();
    }

    private void init() {
        mHeightView = getContext().getResources().getDimensionPixelOffset(R.dimen.frames_video_height);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int minW = getPaddingLeft() + getPaddingRight() + getSuggestedMinimumWidth();
        int w = resolveSizeAndState(minW, widthMeasureSpec, 1);

        final int minH = getPaddingTop() + getPaddingBottom() + mHeightView;
        int h = resolveSizeAndState(minH, heightMeasureSpec, 1);

        setMeasuredDimension(w, h);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        if(w != oldw) {
            getBitmap(w);
        }
    }

    private void getBitmap(final int viewWidth) {
        VidzBackgroundExecutor.execute(new VidzBackgroundExecutor.Task("", 0l, "") {
            @Override
            public void execute() {
                try {
                    LongSparseArray<Bitmap> thumbnailList = new LongSparseArray<>();
                    MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
                    mediaMetadataRetriever.setDataSource(getContext(), mVideoUri);

                    long videoLengthInMs = Integer.parseInt(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)) * 1000;

                    final int thumbWidth = mHeightView;
                    final int thumbHeight = mHeightView;

                    int numThumbs = (int) Math.ceil(((float) viewWidth) / thumbWidth);
                    final long interval = videoLengthInMs / numThumbs;

                    for(int i = 0; i < numThumbs; i++) {
                        Bitmap bitmap = mediaMetadataRetriever.getFrameAtTime(i * interval, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);

                        try {
                            bitmap = Bitmap.createScaledBitmap(bitmap, thumbWidth, thumbHeight, false);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        thumbnailList.put(i, bitmap);
                    }

                    mediaMetadataRetriever.release();
                    returnBitmaps(thumbnailList);
                } catch (final Throwable e) {
                    Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
                }
            }
        });
    }


    private void returnBitmaps(final LongSparseArray<Bitmap> thumbnailList) {
        VidzUiThreadExecutor.runTask("", new Runnable() {
            @Override
            public void run() {
                mBitmapList = thumbnailList;
                invalidate();
            }
        },0l
                );
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(mBitmapList != null) {
            canvas.save();
            int x = 0;

            for(int i = 0; i < mBitmapList.size(); i++) {
                Bitmap bitmap = mBitmapList.get(i);

                if(bitmap != null) {
                    canvas.drawBitmap(bitmap, x, 0, null);
                    x = x + bitmap.getWidth();
                }
            }
        }
    }

    public void setVideo(@NonNull Uri data) {
        mVideoUri = data;
    }

}
