package com.benmohammad.vidz.fragments

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.app.ActivityCompat
import com.benmohammad.vidz.R
import com.benmohammad.vidz.VidzVideoEditor
import com.benmohammad.vidz.interfaces.VidzDialogHelper
import com.benmohammad.vidz.interfaces.VidzFFMpegCallback
import com.benmohammad.vidz.utils.Constants
import com.benmohammad.vidz.utils.VidzUtils
import com.benmohammad.vidz.utils.VidzVideoUtils.secToTime
import com.github.guilhe.views.SeekBarRangedView
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.util.Util
import java.io.File

class VidzAddMusicFragment : VidzBaseCreatorDialogFragment(), VidzDialogHelper, VidzFFMpegCallback {

    private var tagName : String  = VidzAddMusicFragment::class.java.simpleName
    private var audioFile: File? = null
    private var videoFile: File? = null
    private var playWhenReady: Boolean? = false
    private var exoPlayer: SimpleExoPlayer? = null
    private var sbrvVideoTrim: SeekBarRangedView? = null
    private var acbCrop: AppCompatButton? = null
    private var actvStartTime: AppCompatTextView? = null
    private var actvEndTime: AppCompatTextView? = null
    private var ePlayer: PlayerView? = null
    private var acivClose: AppCompatImageView? = null
    private var helper: CallBacks? = null
    private var playbackPosition: Long = 0
    private var currentWindow: Int = 0
    private var playPause: Boolean = false
    private var durationSet: Boolean =  false
    private var flLoadingView: FrameLayout? = null
    private var pbLoading: ProgressBar? = null
    private var tvSelectedAudio: TextView? = null
    private var ivRadio: ImageView? = null
    private var masterAudioFile: File? = null
    private var tvSubTitle: TextView? = null
    private var nextAction: Int = 1
    private var seekToValue: Long = 0L
    private var minSeekValue: Float = 0f
    private var maxSeekValue: Float = 0f
    private var mContext: Context? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val inflate = inflater.inflate(R.layout.vidz_add_music_includer, container, false)
        initView(inflate)
        return inflate
    }

    private fun initView(inflate: View?) {
        flLoadingView = inflate?.findViewById(R.id.flLoadingView)
        acbCrop = inflate?.findViewById(R.id.acbCrop)
        pbLoading = inflate?.findViewById(R.id.pbLoading)
        acivClose = inflate?.findViewById(R.id.acivClose)
        ePlayer = inflate?.findViewById(R.id.ePlayer)
        sbrvVideoTrim = inflate?.findViewById(R.id.sbrvVideoTrim)
        actvStartTime = inflate?.findViewById(R.id.actvStartTime)
        actvEndTime = inflate?.findViewById(R.id.actvEndTime)

        tvSelectedAudio = inflate?.findViewById(R.id.tv_selected_audio)
        ivRadio = inflate?.findViewById(R.id.ivRadio)
        tvSubTitle = inflate?.findViewById(R.id.tvSubTitle)

        flLoadingView?.visibility = View.GONE
        mContext = context

        acivClose?.setOnClickListener {
            dialog?.dismiss()
            helper?.onDidNothing()
        }

        ivRadio?.setOnClickListener {
            checkPermission(Constants.AUDIO_GALLERY, android.Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        tvSelectedAudio?.setOnClickListener {
            checkPermission(Constants.AUDIO_GALLERY, android.Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        sbrvVideoTrim?.setOnSeekBarRangedChangeListener(object: SeekBarRangedView.OnSeekBarRangedChangeListener{
            override fun onChanged(view: SeekBarRangedView?, minValue: Float, maxValue: Float) {
                exoPlayer?.seekTo(minValue.toLong())
            }

            override fun onChanging(view: SeekBarRangedView?, minValue: Float, maxValue: Float) {
                minSeekValue = minValue
                maxSeekValue =  maxValue
                actvStartTime?.text = secToTime(minValue.toLong())
                actvEndTime?.text = secToTime(maxValue.toLong())
            }
        })

        setControls(false)
    }

    private fun releasePlayer() {
        if(exoPlayer != null) {
            playbackPosition = exoPlayer?.currentPosition!!
            currentWindow = exoPlayer?.currentWindowIndex!!
            playWhenReady = exoPlayer?.playWhenReady
            exoPlayer?.release()
            exoPlayer = null
        }
    }

    private fun initializePlayer() {
        exoPlayer = ExoPlayerFactory.newSimpleInstance(
            activity, DefaultRenderersFactory(activity),
            DefaultTrackSelector(), DefaultLoadControl()
        )

        ePlayer?.player = exoPlayer
        exoPlayer?.playWhenReady = false
        exoPlayer?.addListener(playerListener)
    }

    private val playerListener = object : Player.EventListener{
        override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters?) {
         }

        override fun onSeekProcessed() {

        }

        override fun onTracksChanged(
            trackGroups: TrackGroupArray?,
            trackSelections: TrackSelectionArray?
        ) {

        }

        override fun onPlayerError(error: ExoPlaybackException?) {

        }

        override fun onLoadingChanged(isLoading: Boolean) {
            pbLoading?.visibility =  if(isLoading) View.VISIBLE else View.GONE
        }

        override fun onPositionDiscontinuity(reason: Int) {

        }

        override fun onRepeatModeChanged(repeatMode: Int) {

        }

        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {

        }

        override fun onTimelineChanged(timeline: Timeline?, manifest: Any?, reason: Int) {

        }

        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            if(!playWhenReady && playbackState == Player.STATE_READY) {
                if(playWhenReady) {
                    playPause = true
                    activity?.let {

                    }
                } else {
                    playPause = true
                    activity?.let {

                    }
                }

                if(!durationSet) {
                    durationSet = true
                    val duration = exoPlayer?.duration
                    sbrvVideoTrim?.maxValue = duration?.toFloat()!!
                    actvStartTime?.text = secToTime(0)
                    actvEndTime?.text = secToTime(duration)
                    minSeekValue = 0f
                    maxSeekValue = duration.toFloat()
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        releasePlayer()
    }

    override fun setMode(mode: Int) {

    }

    override fun setHelper(helper: CallBacks) {
        this.helper = helper
    }

    override fun setDuration(duration: Long) {
        this.seekToValue = duration
    }

    override fun dismiss() {

    }

    override fun permissionBlocked() {

    }

    override fun onStop() {
        super.onStop()
        if(Util.SDK_INT > 23) {
            releasePlayer()
        }
    }

    companion object {
        fun newInstance() = VidzAddMusicFragment()
    }

    fun checkPermission(requestCode: Int, permission: String) {
        requestPermissions(arrayOf(permission), requestCode)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode) {
            Constants.AUDIO_GALLERY -> {
                for(permission in permissions) {
                    if(ActivityCompat.shouldShowRequestPermissionRationale(activity as Activity, permission)) {

                    } else {
                        if(ActivityCompat.checkSelfPermission(
                                activity as Activity,
                                permission
                            ) == PackageManager.PERMISSION_GRANTED) {
                            val i = Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
                            i.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("audio/*", "video/*"))
                            startActivityForResult(i, Constants.AUDIO_GALLERY)

                        } else {
                            val intent = Intent()
                            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            val uri = Uri.fromParts("package", context!!.applicationContext.packageName, null)
                            intent.data = uri
                            startActivityForResult(intent, 300)
                        }
                    }
                }
                return
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_CANCELED) return

        when(requestCode) {
            Constants.AUDIO_GALLERY -> {
                data?.let {
                    setFilePath(resultCode, it, Constants.AUDIO_GALLERY)
                }
            }
        }
    }

    private fun setFilePath(resultCode: Int, data: Intent, mode: Int) {
        if(resultCode == Activity.RESULT_OK) {
            try {
                val selectedImage = data.data

                val filePathColumn = arrayOf(MediaStore.MediaColumns.DATA)
                val cursor = context!!.contentResolver.query(selectedImage!!, filePathColumn, null, null, null)
                if(cursor != null) {
                    cursor.moveToFirst()
                    val columnIndex = cursor.getColumnIndex(filePathColumn[0])
                    val filePath = cursor.getString(columnIndex)
                    cursor.close()
                    if(mode == Constants.AUDIO_GALLERY) {
                        masterAudioFile = File(filePath)
                        masterAudioFile?.let {
                            file -> tvSelectedAudio!!.text = masterAudioFile!!.name.toString()

                            setControls(true)

                            if(Util.SDK_INT <= 23 || exoPlayer == null) {
                                initializePlayer()
                            }
                        }
                    }
                }
            }catch (e: Exception) {

            }
        }
    }


    private fun setControls(isShow: Boolean) {
        if(isShow) {
            tvSubTitle!!.visibility = View.VISIBLE
            sbrvVideoTrim!!.visibility = View.VISIBLE
            actvStartTime!!.visibility = View.VISIBLE
            actvEndTime!!.visibility = View.VISIBLE
            acbCrop!!.visibility = View.VISIBLE
        } else {
            tvSubTitle!!.visibility = View.INVISIBLE
            sbrvVideoTrim!!.visibility = View.INVISIBLE
            actvStartTime!!.visibility = View.INVISIBLE
            actvEndTime!!.visibility = View.INVISIBLE
            acbCrop!!.visibility = View.INVISIBLE
        }
    }

    override fun setFilePathFromSource(file: File) {
        videoFile = file
        Log.d("getMimeType= ","" + getMimeType(videoFile!!.absolutePath))
    }

    override fun onProgress(progress: String) {
        Log.v(tagName, "onProgress()")
        if(nextAction == 1) {
            flLoadingView?.visibility = View.VISIBLE
            acbCrop?.visibility = View.GONE
        }
    }

    override fun onSuccess(convertedFile: File, type: String) {
        Log.v(tagName, "onSuccess()")
        if(nextAction == 1) {
            flLoadingView?.visibility = View.GONE
            acbCrop?.visibility = View.VISIBLE
            audioFile = convertedFile
        } else {
            helper?.showLoading(false)
            helper?.onFileProcessed(convertedFile)
        }
    }

    override fun onFailure(error: Exception) {
        Log.v(tagName, "onFailure() ${error.localizedMessage}")
        Toast.makeText(mContext, "VideoProcessing" , Toast.LENGTH_SHORT).show()
        if(nextAction == 1) {
            flLoadingView?.visibility = View.GONE
            acbCrop?.visibility = View.VISIBLE
        } else {
            helper?.showLoading(false)
        }
    }

    override fun onNotAvailable(error: Exception) {
        Log.v(tagName, "onNotAvailable() ${error.localizedMessage}")
    }

    override fun onFinish() {
        Log.v(tagName, "onFinish()")
        if(nextAction == 1) {
            flLoadingView?.visibility = View.GONE
            acbCrop?.visibility = View.VISIBLE
            releasePlayer()
            muxVideoPlayer()
        } else {
            helper?.showLoading(false)
            dialog?.dismiss()
        }
    }

    private fun muxVideoPlayer() {
        stopRunningProcess()

        if(!isRunning()) {
            val outputFile = VidzUtils.createVideoFile(context!!)
            Log.v(tagName, "outputFile: ${outputFile.absolutePath}")

            nextAction = 2

            VidzVideoEditor.with(context!!)
                .setType(Constants.VIDEO_AUDIO_MERGE)
                .setFile(videoFile!!)
                .setAudioFile(audioFile!!)
                .setOutputPath(outputFile.path)
                .setCallback(this)
                .main()

            helper?.showLoading(true)
        } else {
            showInProgressToast()
        }
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        releasePlayer()
    }

}