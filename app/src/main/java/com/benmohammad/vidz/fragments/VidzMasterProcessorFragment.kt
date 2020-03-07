package com.benmohammad.vidz.fragments

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.benmohammad.vidz.R
import com.benmohammad.vidz.adapter.VidzVideoOptionsAdapter
import com.benmohammad.vidz.interfaces.VidzVideoOptionsListener
import com.benmohammad.vidz.interfaces.VidzBaseCreatorDialogFragment
import com.benmohammad.vidz.interfaces.VidzFFMpegCallback
import com.benmohammad.vidz.utils.*
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
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

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

        checkStoragePermission(Constants.PERMISSION_STORAGE)

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
                        val outputFile = createSaveVideoFile()
                        VidzCommonMethods.copyFile(masterVideoFile, outputFile)
                        Toast.makeText(context, R.string.successfully_saved, Toast.LENGTH_SHORT).show()

                        tvSave!!.visibility = View.GONE

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
            convertAviToMp4()
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

    private fun checkStoragePermission(permission: Array<String>) {
        val blockedPermission = checkHasPermission(activity, permission)
        if(blockedPermission != null && blockedPermission.size > 0) {
            val isBlocked = isPermissionBlocked(activity, blockedPermission)
            if(isBlocked) {
                callPermissionSettings()
            } else {
                requestPermissions(permission, Constants.ADD_ITEMS_IN_STORAGE)
            }
        } else {
            itemStorageAction()
        }
    }
    private fun callPermissionSettings() {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        val uri = Uri.fromParts("package", context!!.applicationContext.packageName, null)
        intent.data = uri

        startActivityForResult(intent, 300)
    }

    private fun itemStorageAction() {
        val sessionManager = VidzSessionManager()

        if(sessionManager.isFirstTime(activity!!)) {
            VidzUtils.copyFileToInternalStorage(
                R.drawable.sticker_1,
                "sticker_1",
                context!!
            )

            VidzUtils.copyFileToInternalStorage(
                R.drawable.sticker_2,
                "sticker_2",
                context!!
            )

            VidzUtils.copyFileToInternalStorage(
                R.drawable.sticker_3,
                "sticker_3",
                context!!
            )

            VidzUtils.copyFileToInternalStorage(
                R.drawable.sticker_4,
                "sticker_4",
                context!!
            )

            VidzUtils.copyFileToInternalStorage(
                R.drawable.sticker_5,
                "sticker_5",
                context!!
            )

            sessionManager.setFirstTime(activity!!, false)
        }
    }

    private var isFirstTimePermission: Boolean
    get() = preferences.getBoolean("isFirstTimePermission", false)
    set(isFirstTime) = preferences.edit().putBoolean("isFirstTimePermission", isFirstTime).apply()

    private val isMarshMallow: Boolean
    get() =(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) or (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP_MR1)

    fun checkHasPermission(context: Activity?, permissions: Array<String>?) : ArrayList<String> {
        permissionList = ArrayList()
        if(isMarshMallow && context != null && permissions != null) {
            for(permission in permissions) {
                if(ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    permissionList.add(permission)
                }
            }
        }

        return permissionList
    }

    fun isPermissionBlocked(context: Activity?, permissions: ArrayList<String>?) : Boolean {
        if(isMarshMallow && context != null && permissions != null && isFirstTimePermission) {
            for(permission in permissions) {
                if(!ActivityCompat.shouldShowRequestPermissionRationale(context, permission)) {
                    return true
                }
            }
        }

        return false
    }


    private fun createSaveVideoFile(): File {
        val timeStamp: String = SimpleDateFormat(Constants.DATE_FORMAT, Locale.getDefault()).format(Date())
        val imageFileName: String = Constants.APP_NAME + timeStamp+ "_"
        val path = Environment.getExternalStorageDirectory().toString()+ File.separator + Constants.APP_NAME + File.separator + Constants.MY_VIDEOS + File.separator
        val folder = File(path)
        if(!folder.exists())
            folder.mkdirs()

        return File.createTempFile(imageFileName, Constants.VIDEO_FORMAT, folder)
    }

    private fun convertAviToMp4() {
        AlertDialog.Builder(context!!)
            .setTitle(Constants.APP_NAME)
            .setMessage(getString(R.string.not_supported_video))
            .setPositiveButton(getString(R.string.yes)) {
                dialog, which -> val outputFile = VidzUtils.createVideoFile(context!!)
                Log.v(tagName, "outputFile: $outputFile")




            }
    }

}