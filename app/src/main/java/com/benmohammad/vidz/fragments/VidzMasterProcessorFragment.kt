package com.benmohammad.vidz.fragments

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.benmohammad.vidz.R
import com.benmohammad.vidz.adapter.VidzVideoOptionsAdapter
import com.benmohammad.vidz.interfaces.VidzVideoOptionsListener
import com.benmohammad.vidz.interfaces.VidzBaseCreatorDialogFragment
import com.benmohammad.vidz.interfaces.VidzFFMpegCallback
import com.benmohammad.vidz.utils.Constants
import com.benmohammad.vidz.utils.VideoFrom
import com.benmohammad.vidz.utils.VidzCommonMethods
import com.benmohammad.vidz.utils.VidzVideoUtils
import com.github.hiteshsondhi88.libffmpeg.FFmpeg
import com.github.hiteshsondhi88.libffmpeg.FFmpegLoadBinaryResponseHandler
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.ui.PlayerView
import org.jcodec.movtool.Util
import java.io.File

class VidzMasterProcessorFragment : Fragment(), VidzBaseCreatorDialogFragment.CallBacks,
    VidzVideoOptionsListener, VidzFFMpegCallback {



    private lateinit var rootView: View
    private var tagName: String = VidzMasterProcessorFragment::class.java.simpleName
    private var videoUri: Uri? = null
    private var videoFile: File? = null
    private var permissionList: ArrayList<String> = ArrayList()
    private lateinit var preferences : SharedPreferences
    private lateinit var progresssBar: ProgressBar
    private var tvVideoProcessing: TextView? = null
    private var handler : Handler = Handler()
    private var ibGallery: ImageButton? = null
    private var ibCamera : ImageButton? = null
    private var masterVideoFile: File? = null
    private var playbackPosition: Long = 0;
    private var currentWindow: Int = 0
    private var ePlayer: PlayerView? = null
    private var pbLoading: ProgressBar? = null
    private var exoplayer : SimpleExoPlayer? = null
    private var playWhenReady: Boolean? = false
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var rvVideoOptions: RecyclerView
    private lateinit var vidzVideoOptionsAdapter: VidzVideoOptionsAdapter
    private var videoOptions: ArrayList<String> = ArrayList()
    private var orientationLand: Boolean = false
    private var tvSave: ImageView? = null
    private var isLargeVideo: Boolean? = false
    private var mContext: Context? = null
    private var tvInfo: TextView? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.vidz_processor_fragment, container, false);
        initView(rootView)
        return rootView

    }

    private fun initView(rootView: View?    ) {
        ePlayer = rootView?.findViewById(R.id.ePlayer)
        tvSave = rootView?.findViewById(R.id.tvSave)
        pbLoading = rootView?.findViewById(R.id.pbLoading)
        ibGallery = rootView?.findViewById(R.id.ibGallery)
        ibCamera = rootView?.findViewById(R.id.ibCamera)
        progresssBar = rootView?.findViewById(R.id.progressBar)!!
        tvVideoProcessing = rootView.findViewById(R.id.tvVideoProcessing)
        tvInfo = rootView?.findViewById(R.id.tvInfo)

        preferences =  activity!!.getSharedPreferences("fetch_preferences", Context.MODE_PRIVATE)

        rvVideoOptions = rootView?.findViewById(R.id.rvVideoOptions)
        linearLayoutManager =  LinearLayoutManager(activity!!.applicationContext)
        linearLayoutManager.orientation = LinearLayoutManager.HORIZONTAL

        rvVideoOptions.layoutManager =  linearLayoutManager
        mContext = context
        videoOptions.add(Constants.TRIM)
        videoOptions.add(Constants.MUSIC)
        videoOptions.add(Constants.PLAYBACK)
        videoOptions.add(Constants.TEXT)
        videoOptions.add(Constants.OBJECT)
        videoOptions.add(Constants.MERGE)

        vidzVideoOptionsAdapter = VidzVideoOptionsAdapter(videoOptions, activity!!.applicationContext, this, orientationLand)
        rvVideoOptions.adapter = vidzVideoOptionsAdapter
        vidzVideoOptionsAdapter.notifyDataSetChanged()

        //checkStoragePermission(Constants.PERMISSION_STORAGE)

        try {
            FFmpeg.getInstance(activity).loadBinary(object: FFmpegLoadBinaryResponseHandler {
                override fun onFailure() {
                    Log.v("FFmpeg", "Failed to load FFmpeg library")
                }

                override fun onSuccess() {
                    Log.v("FFmpeg", "FFmpeg library loaded")
                }

                override fun onStart() {
                    Log.v("FFmpeg", "FFmpeg started")
                }

                override fun onFinish() {
                    Log.v("FFmpeg", "FFmpeg stopped")
                }
            })
        } catch (e: FFmpegNotSupportedException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        ibGallery?.setOnClickListener {
            openGallery()
        }

        ibCamera?.setOnClickListener {
            openCamera()
        }

        tvSave?.setOnClickListener {
            AlertDialog.Builder(context!!)
                .setTitle(Constants.APP_NAME)
                .setMessage(getString(R.string.save_video))
                .setPositiveButton(getString(R.string.Continue)) {
                    dialog, which ->
                    if(masterVideoFile != null) {
//                        val outputFile = createSaveVideoFile()
//                        VidzCommonMethods.copyFile(masterVideoFile, outputFile)
//                        Toast.makeText(context, R.string.successfully_saved, Toast.LENGTH_SHORT).show()
//
//                        tvSave!!.visibility = View.GONE

                    }
                }
                .setNegativeButton(R.string.cancel) {dialog, which ->  }
                .show()
        }

        tvInfo?.setOnClickListener {
            VidzVideoOptionFragment.newInstance().apply {
                setHelper(this@VidzMasterProcessorFragment)
            }.show(fragmentManager!!, "VidzVideoOptionFragment")
        }

    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if(newConfig!!.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Log.v(tagName, "orientation: ORIENTATION LANDSCAPE")
            orientationLand = true
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            Log.v(tagName, "orientation: ORIENTATION PORTRAIT")
            orientationLand = false
        }

        vidzVideoOptionsAdapter = VidzVideoOptionsAdapter(videoOptions, activity!!.applicationContext, this, orientationLand)
        rvVideoOptions.adapter = vidzVideoOptionsAdapter
        vidzVideoOptionsAdapter.notifyDataSetChanged()
    }

    override fun onDidNothing() {
        initializePlayer()
    }

    override fun onFileProcessed(file: File) {
        tvSave!!.visibility = View.VISIBLE
        masterVideoFile = file
        isLargeVideo = false

        val extension = VidzCommonMethods.getFileExtension(masterVideoFile!!.absolutePath)

        if(extension == Constants.AVI_FORMAT) {
            //convertAviToMp4()
        } else {
            playbackPosition = 0
            currentWindow = 0
            initializePlayer()
        }
    }

    override fun getFile(): File? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun reInitPlayer() {
        initializePlayer()
    }

    override fun onAudioFileProcessed(convertedAudioFile: File) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun showLoading(isShow: Boolean) {
        if(isShow) {
            progresssBar.visibility = View.VISIBLE
            tvVideoProcessing!!.visibility = View.VISIBLE
            //setProgressValue()
        } else {
            progresssBar.visibility = View.INVISIBLE
            tvVideoProcessing!!.visibility = View.INVISIBLE
        }
    }

    override fun openGallery() {


    }

    override fun openCamera() {


    }

    override fun videoOption(option: String) {


    }

    override fun onProgress(progress: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onSuccess(convertedFile: File, type: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onFailure(error: Exception) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onNotAvailable(error: Exception) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onFinish() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun initializePlayer() {
        try {
            tvInfo!!.visibility = View.GONE

            ePlayer?.useController = true
            exoplayer = ExoPlayerFactory.newSimpleInstance(
                activity,
                DefaultRenderersFactory(activity),
                DefaultTrackSelector(), DefaultLoadControl())

            ePlayer?.player = exoplayer
            exoplayer?.playWhenReady = false
            exoplayer?.addListener(playerListener)

            exoplayer?.prepare(VidzVideoUtils.buildMediaSource(Uri.fromFile(masterVideoFile), VideoFrom.LOCAL))

            exoplayer?.seekTo(0)

            exoplayer?.seekTo(currentWindow, playbackPosition)
        } catch (exception: Exception) {
            Log.v(tagName, "exception: " + exception.localizedMessage)
        }
    }

    private val playerListener = object : Player.EventListener {
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
            Log.v(tagName, "onPlayerError: ${error.toString()}")
            Toast.makeText(mContext, "Video format is not supported", Toast.LENGTH_LONG).show()

        }

        override fun onLoadingChanged(isLoading: Boolean) {
            pbLoading?.visibility = if (isLoading) View.VISIBLE else View.GONE
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
            if(playWhenReady && playbackState == Player.STATE_READY) {

            } else if(playWhenReady) {

            } else {

            }
        }
    }

}