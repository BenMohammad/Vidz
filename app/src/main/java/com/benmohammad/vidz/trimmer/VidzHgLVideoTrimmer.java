package com.benmohammad.vidz.trimmer;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.renderscript.RenderScript;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.InputDevice;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;

import com.benmohammad.vidz.R;
import com.benmohammad.vidz.trimmer.interfaces.VidzOnHgLVideoListener;
import com.benmohammad.vidz.trimmer.interfaces.VidzOnProgressVideoListener;
import com.benmohammad.vidz.trimmer.interfaces.VidzOnRangeSeekBarListener;
import com.benmohammad.vidz.trimmer.interfaces.VidzOnTrimVideoListener;
import com.benmohammad.vidz.trimmer.utils.VidzBackgroundExecutor;
import com.benmohammad.vidz.trimmer.utils.VidzUiThreadExecutor;
import com.benmohammad.vidz.trimmer.view.VidzProgressBarView;
import com.benmohammad.vidz.trimmer.view.VidzRangeSeekBarView;
import com.benmohammad.vidz.trimmer.view.VidzThumb;
import com.benmohammad.vidz.trimmer.view.VidzTimeLineView;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import static com.benmohammad.vidz.trimmer.utils.VidzTrimVideoUtils.stringForTime;

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
    private VidzTimeLineView mTimeLineView;

    private VidzProgressBarView mVideoProgressIndicator;
    private Uri mSrc;
    private String mFinalPath;
    private int mMaxDuration;
    private List<VidzOnProgressVideoListener> mListeners;

    private VidzOnTrimVideoListener mOnTrimVideoListener;
    private VidzOnHgLVideoListener mOnHgLVideoListener;

    private int mDuration = 0;
    private int mTimeVideo = 0;
    private int mStartPosition = 0;
    private int mEndPosition = 0;

    private long mOriginalSizeFile;
    private boolean mResetSeekBar = true;
    private final MessageHandler messageHandler = new MessageHandler(this);

    public VidzHgLVideoTrimmer(@NonNull Context context, AttributeSet attrs) {
        super(context, attrs, 0);
    }

    public VidzHgLVideoTrimmer(@NonNull Context context, AttributeSet attr, int defStyleAttr) {
        super(context, attr, defStyleAttr);
        init(context);
    }

    private void init(Context context) {

        LayoutInflater.from(context).inflate(R.layout.vidz_view_time_line, this, true);

        mHolderTopView =  findViewById(R.id.handlerTop);
        mVideoProgressIndicator = findViewById(R.id.timeVideoView);
        mRangeSeekBarView = findViewById(R.id.timeLineBar);
        mlinearVideo = findViewById(R.id.surface_view);
        mVideoView = findViewById(R.id.video_loader);
        mPlayView = findViewById(R.id.icon_video_play);
        mTimeInfoContainer = findViewById(R.id.timeText);
        mTextSize = findViewById(R.id.textSize);
        mTextTimeFrame = findViewById(R.id.textTimeSelection);
        mTextTime = findViewById(R.id.textTime);
        mTimeLineView = findViewById(R.id.timeLineView);

        setUpListeners();
        setUpMargins();

    }

    private void setUpListeners() {
        mListeners = new ArrayList<>();
        mListeners.add((time, max, scale) -> updateVideoProgress(time));
        mListeners.add(mVideoProgressIndicator);

        findViewById(R.id.btCancel).setOnClickListener(
                view -> onCancelClicked()
        );

        findViewById(R.id.btSave).setOnClickListener(
                view -> onSaveClicked()
        );

        final GestureDetector gestureDetector = new GestureDetector(getContext(),
                new GestureDetector.SimpleOnGestureListener(){
                    @Override
                    public boolean onSingleTapConfirmed(MotionEvent e) {
                        onClickVideoPlayPause();
                        return true;
                    }
                });

        mVideoView.setOnErrorListener((mediaPLayer, what, extra) -> {
            if(mOnTrimVideoListener != null)
                mOnTrimVideoListener.onError("Error reason: " +what);
            return false;
        });

        mVideoView.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            return true;
        });

        mRangeSeekBarView.addOnSeekBarListener(mVideoProgressIndicator);
        mRangeSeekBarView.addOnSeekBarListener(new VidzOnRangeSeekBarListener() {
            @Override
            public void onCreate(VidzRangeSeekBarView rangeSeekBarView, int index, float value) {

            }

            @Override
            public void onSeek(VidzRangeSeekBarView rangeSeekBarView, int index, float value) {
                onSeekThumbs(index, value);
            }

            @Override
            public void onSeekStart(VidzRangeSeekBarView rangeSeekBarView, int index, float value) {

            }

            @Override
            public void onSeekStop(VidzRangeSeekBarView rangeSeekBarView, int index, float value) {
                onStopSeekThumbs();
            }
        });

        mHolderTopView.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                onPlayerIndicatorSeekChanged(progress, fromUser);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                onPlayerIndicatorSeekStart();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                onPlayerIndicatorSeekStop(seekBar);
            }
        });

        mVideoView.setOnPreparedListener(this::onVideoPrepared);
        mVideoView.setOnCompletionListener(mp -> onVideoCompleted());
    }


    private void setUpMargins() {
        int marge =mRangeSeekBarView.getThumbs().get(0).getWidthBitmap();
        int widthSeek = mHolderTopView.getThumb().getMinimumWidth() / 2;

        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mHolderTopView.getLayoutParams();
        lp.setMargins(marge - widthSeek, 0, marge - widthSeek, 0 );
        mHolderTopView.setLayoutParams(lp);

        lp = (RelativeLayout.LayoutParams) mTimeLineView.getLayoutParams();
        lp.setMargins(marge, 0, marge, 0);
        mTimeLineView.setLayoutParams(lp);

        lp = (RelativeLayout.LayoutParams) mVideoProgressIndicator.getLayoutParams();
        lp.setMargins(marge, 0, marge, 0);
        mVideoProgressIndicator.setLayoutParams(lp);
    }

    private void onSaveClicked() {

        int finalDuration = mEndPosition - mStartPosition;

        if(finalDuration < 240000) {
            mPlayView.setVisibility(View.VISIBLE);
            mVideoView.pause();

            if(mOnTrimVideoListener != null) {
                Log.v(TAG, "mStartPosition: " + mStartPosition + " mEndPosition: " + mEndPosition);
                mOnTrimVideoListener    .onTrimStarted(mStartPosition, mEndPosition);
            } else {
                Toast.makeText(getContext(), "Please trim video", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void onClickVideoPlayPause() {
        if(mVideoView.isPlaying()) {
            mPlayView.setVisibility(View.VISIBLE);
            messageHandler.removeMessages(SHOW_PROGRESS_);
            mVideoView.pause();
        } else {
            mPlayView.setVisibility(GONE);

            if(mResetSeekBar) {
                mResetSeekBar = false;
                mVideoView.seekTo(mStartPosition);
            }

            messageHandler.sendEmptyMessage(SHOW_PROGRESS_);
            mVideoView.start();
        }
    }

    private void onCancelClicked() {
        mVideoView.stopPlayback();
        if(mOnTrimVideoListener != null) {
            mOnTrimVideoListener.cancelAction();
        }
    }


    private void onPlayerIndicatorSeekChanged(int progress, boolean fromUser) {
        int duration = (int) ((mDuration * progress) / 1000);

        if(fromUser) {
            if(duration < mStartPosition) {
                setProgressBarPosition(mStartPosition);
                duration = mStartPosition;
            } else if (duration > mEndPosition) {
                setProgressBarPosition(mEndPosition);
                duration = mEndPosition;
            }
            setTimeVideo(duration);
        }
    }

    private void onPlayerIndicatorSeekStart() {
        messageHandler.removeMessages(SHOW_PROGRESS_);
        mVideoView.pause();
        mPlayView.setVisibility(View.VISIBLE);
        notifyProgressUpdate(false);
    }

    private void onPlayerIndicatorSeekStop(@NonNull SeekBar seekbar) {
        messageHandler.removeMessages(SHOW_PROGRESS_);
        mVideoView.pause();

        int duration = (int) ((mDuration * seekbar.getProgress()) / 1000L);
        mVideoView.seekTo(duration);
        setTimeVideo(duration);
        notifyProgressUpdate(false);
    }

    private void onVideoPrepared(@NonNull MediaPlayer mp) {

        int videoWidth = mp.getVideoWidth();
        int videoHeight = mp.getVideoHeight();
        float videoProportion = (float) videoWidth / (float) videoHeight;
        int screenWidth = mlinearVideo.getWidth();
        int screenHeight = mlinearVideo.getHeight();

        float screenProportion = (float) screenWidth / (float) screenHeight;
        ViewGroup.LayoutParams lp = mVideoView.getLayoutParams();

        if(videoProportion > screenProportion) {
            lp.width = screenWidth;
            lp.height = (int) ((float) screenWidth / videoProportion);
        } else {
            lp.width = (int) (videoProportion * (float) screenHeight);
            lp.height = screenHeight;
        }

        mVideoView.setLayoutParams(lp);
        mPlayView.setVisibility(View.VISIBLE);
        mDuration = mVideoView.getDuration();
        setSeekBarPosition();

        setTimeFrames();
        setTimeVideo(0);

        if(mOnHgLVideoListener != null) {
            mOnHgLVideoListener.onVideoPrepared();
        }
    }


    private void setSeekBarPosition() {
        if(mDuration >= mMaxDuration) {
            mStartPosition = mDuration / 2 - mMaxDuration / 2;
            mEndPosition = mDuration / 2 + mMaxDuration / 2;

            mRangeSeekBarView.setThumbValue(0, (mStartPosition * 100) / mDuration);
            mRangeSeekBarView.setThumbValue(1, (mEndPosition * 100) / mDuration);
        } else {
            mStartPosition = 0;
            mEndPosition = mDuration;
        }

        setProgressBarPosition(mStartPosition);
        mVideoView.seekTo(mStartPosition);

        mTimeVideo = mDuration;
        mRangeSeekBarView.initMaxWidth();
    }

    private void setTimeFrames() {
        String seconds = getContext().getString(R.string.short_seconds);
        mTextTimeFrame.setText(String.format("%s %s - %s %s", stringForTime(mStartPosition), seconds, stringForTime(mEndPosition), seconds));

    }

    private void setTimeVideo(int position) {
        String seconds = getContext().getString(R.string.short_seconds);
        mTextTimeFrame.setText(String.format("%s %s", stringForTime(position), seconds));
    }

    private void onSeekThumbs(int index, float value) {
        switch(index) {
            case VidzThumb.LEFT: {
                mStartPosition = (int) ((mDuration * value) / 100L);
                mVideoView.seekTo(mStartPosition);
                break;
            }
                case VidzThumb.RIGHT: {
                    mEndPosition = (int) ((mDuration * value) / 100L);
                    break;
        }
        }

        setProgressBarPosition(mStartPosition);

        setTimeFrames();
        mTimeVideo = mEndPosition - mStartPosition;

    }

    private void onStopSeekThumbs() {
        messageHandler.removeMessages(SHOW_PROGRESS_);
        mVideoView.pause();
        mPlayView.setVisibility(VISIBLE);
    }

    private void onVideoCompleted() {
        mVideoView.seekTo(mStartPosition);
    }

    private void notifyProgressUpdate(boolean all) {
        if(mDuration == 0) return;

        int position = mVideoView.getCurrentPosition();
        if(all) {
            for(VidzOnProgressVideoListener item : mListeners) {
                item.updateProgress(position, mDuration, ((position * 100) / mDuration));
            }
        } else {
            mListeners.get(1).updateProgress(position, mDuration, ((position * 100) / mDuration));
        }
    }

    private void updateVideoProgress(int time) {
        if(mVideoView == null) {
            return;
        }

        if(time >= mEndPosition) {
            messageHandler.removeMessages(SHOW_PROGRESS_);
            mVideoView.pause();
            mPlayView.setVisibility(VISIBLE);
            mResetSeekBar = true;
            return;
        }

        if(mHolderTopView != null) {
            setProgressBarPosition(time);
        }

        setTimeVideo(time);
    }


    private void setProgressBarPosition(int position) {
        if(mDuration > 0) {
            long pos = 1000L * position / mDuration;
            mHolderTopView.setProgress((int) pos);
        }
    }

    public void setVideoInformationVisibility(boolean visible) {
        mTimeInfoContainer.setVisibility(visible ? VISIBLE : GONE );
    }

    public void setOnTrimVideoListener(VidzOnTrimVideoListener onTrimVideoListener) {
        mOnTrimVideoListener = onTrimVideoListener;
    }

    public void setOnHgLVideoListener(VidzOnHgLVideoListener onHgLVideoListener) {
        mOnHgLVideoListener = onHgLVideoListener;
    }

    public void destroy() {
        VidzBackgroundExecutor.cancelAll("", true);
        VidzUiThreadExecutor.cancelAll("");
    }

    public void setMaxDuration(int maxDuration) {
        mMaxDuration = maxDuration;
    }

    public void setVideoUri(final Uri videoUri) {
        mSrc = videoUri;
        if(mOriginalSizeFile == 0) {
            File file = new File(mSrc.getPath());
            mOriginalSizeFile = file.length();

            long fileSizeInKb = mOriginalSizeFile / 1024;

            if(fileSizeInKb > 1000) {
                long fileSizeInMb = fileSizeInKb / 1024;
                mTextSize.setText(String.format("%s %s", fileSizeInMb, getContext().getString(R.string.megabyte)));
            } else {
                mTextSize.setText(String.format("%s %s", fileSizeInKb, getContext().getString(R.string.kilobyte)));
            }
        }

        mVideoView.setVideoURI(mSrc);
        mVideoView.requestFocus();

        mTimeLineView.setVideo(mSrc);
    }





    private static class MessageHandler extends Handler {
        @NonNull
        private final WeakReference<VidzHgLVideoTrimmer> mView;

        MessageHandler(VidzHgLVideoTrimmer view) {
            mView = new WeakReference<>(view);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            VidzHgLVideoTrimmer view = mView.get();
            if(view == null || view.mVideoView == null) {
                return;
            }

            view.notifyProgressUpdate(true);
            if(view.mVideoView.isPlaying()) {
                sendEmptyMessageDelayed(0, 10);
            }

        }
    }
}
