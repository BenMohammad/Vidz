package com.benmohammad.vidz.fragments

import android.Manifest
import android.app.Activity
import android.app.Activity.RESULT_OK
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
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.benmohammad.vidz.R
import com.benmohammad.vidz.VidzTrimmerActivity
import com.benmohammad.vidz.VidzVideoEditor
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
import com.google.android.exoplayer2.util.Util.*
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
        tvInfo = rootView.findViewById(R.id.tvInfo)

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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode) {
            Constants.VIDEO_GALLERY -> {
                for(permission in permissions) {
                    if(ActivityCompat.shouldShowRequestPermissionRationale(activity as Activity, permission)) {
                        Toast.makeText(context, "Permission Denied", Toast.LENGTH_SHORT).show()
                        break
                    } else {
                        if(ActivityCompat.checkSelfPermission(
                                activity as Activity,
                                permission
                            ) == PackageManager.PERMISSION_GRANTED) {
                            VidzUtils.refreshGalleryAlone(context!!)
                            val i = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
                            i.type = "video/*"
                            i.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("video/*"))
                            startActivityForResult(i, Constants.VIDEO_GALLERY)
                        } else {
                            callPermissionSettings()
                        }
                    }
                }
                return
            }

            Constants.AUDIO_GALLERY -> {
                for(permission in permissions) {
                    if(ActivityCompat.shouldShowRequestPermissionRationale(activity as Activity, permission)) {
                        Toast.makeText(context, "Permission Denied!!", Toast.LENGTH_SHORT).show()
                    } else {
                        if(ActivityCompat.checkSelfPermission(
                                activity as Activity,  permission
                            ) == PackageManager.PERMISSION_GRANTED) {
                            VidzUtils.refreshGalleryAlone(context!!)
                            val i = Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
                            i.type = "video/*"
                            i.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("video/*"))
                            startActivityForResult(i, Constants.AUDIO_GALLERY)
                        } else {
                            callPermissionSettings()
                        }
                    }
                }
                return
            }

            Constants.RECORD_VIDEO -> {
                for(permission in permissions) {
                    if(ActivityCompat.shouldShowRequestPermissionRationale(activity as Activity, permission)) {
                        Toast.makeText(context, "Permission Denied!!!", Toast.LENGTH_SHORT).show()
                        break
                    } else {
                        if(ActivityCompat.checkSelfPermission(
                                context!!,
                                permission
                            ) == PackageManager.PERMISSION_GRANTED) {

                            val cameraIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
                            videoFile = VidzUtils.createVideoFile(context!!)
                            Log.v(tagName, "videoPath: ${videoFile!!.absolutePath}")
                            videoUri = FileProvider.getUriForFile(
                                context!!,
                                "com.benmohammad.vidz.provider",
                                videoFile!!
                            )

                            cameraIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 240)
                            cameraIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1)
                            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, videoFile)
                            startActivityForResult(cameraIntent, Constants.RECORD_VIDEO)
                        } else {
                                callPermissionSettings()
                        }
                    }
                }
                return
            }

            Constants.ADD_ITEMS_IN_STORAGE -> {
                for(permission in permissions) {
                    if(ActivityCompat.shouldShowRequestPermissionRationale(activity as Activity, permission)) {
                        Toast.makeText(context, "Permission denied!!", Toast.LENGTH_SHORT).show()
                        break
                    } else {
                        if(ActivityCompat.checkSelfPermission(
                                context!!,
                                permission
                            ) == PackageManager.PERMISSION_GRANTED) {
                            itemStorageAction()
                        } else {
                            callPermissionSettings()
                        }
                    }
                }
                return
            }

        }
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

    fun checkAllPermission(permission: Array<String>) {
        val blockedPermission = checkHasPermission(activity, permission)
        if(blockedPermission != null && blockedPermission.size > 0) {
            val isBlocked = isPermissionBlocked(activity, blockedPermission)
            if(isBlocked) {
                callPermissionSettings()
            } else {
                requestPermissions(permission, Constants.RECORD_VIDEO)
            }
        } else {
            val cameraIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
            videoFile = VidzUtils.createVideoFile(context!!)
            Log.v(tagName, "videoPath1: " +videoFile!!.absolutePath)
            videoUri = FileProvider.getUriForFile(context!!, "com.benmohammad.vidz.provider", videoFile!!)
            cameraIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 240)
            cameraIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1)
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, videoFile)
            startActivityForResult(cameraIntent, Constants.RECORD_VIDEO)
        }

    }

    fun checkPermission(requestCode: Int, permission: String) {
        requestPermissions(arrayOf(permission), requestCode)
    }

    override fun openGallery() {
        releasePlayer()
        checkPermission(Constants.VIDEO_GALLERY, Manifest.permission.READ_EXTERNAL_STORAGE)

    }

    override fun openCamera() {
        releasePlayer()
        checkAllPermission(Constants.PERMISSION_CAMERA)

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

    private fun setFilePath(resultCode: Int, data: Intent, mode: Int) {
        if(resultCode == RESULT_OK) {
            try {
                val selectedImage = data.data
                val filePathColumn = arrayOf(MediaStore.MediaColumns.DATA)
                val cursor = context!!.contentResolver
                    .query(selectedImage!!, filePathColumn, null, null, null)
                if(cursor != null) {
                    cursor.moveToFirst()
                    val columnIndex = cursor
                        .getColumnIndex(filePathColumn[0])
                    cursor.close()
                    val filePath = cursor.getString(columnIndex)
                    if(mode == Constants.VIDEO_GALLERY) {
                        Log.v(tagName, "filepath: $filePath")
                        masterVideoFile = File(filePath)

                        val extension = VidzCommonMethods.getFileExtension(masterVideoFile!!.absolutePath)

                        val timeInMillis = VidzUtils.getVideoDuration(context!!, masterVideoFile!!)
                        Log.v(tagName, "timeInMillis: $timeInMillis")
                        val duration = VidzCommonMethods.convertDurationInMinutes(timeInMillis)
                        Log.v(tagName, "Video duration: $duration")


                        if(duration < Constants.VIDEO_LIMIT) {
                            if(extension == Constants.AVI_FORMAT) {
                                convertAviToMp4()
                            } else {
                                playbackPosition = 0
                                currentWindow = 0
                                initializePlayer()
                            }
                        } else {
                            Toast.makeText(activity, getString(R.string.error_select_smaller_video), Toast.LENGTH_SHORT).show()

                            isLargeVideo = true
                            val uri = Uri.fromFile(masterVideoFile)
                            val intent = Intent(context, VidzTrimmerActivity::class.java)
                            intent.putExtra("VideoPath", filePath)
                            intent.putExtra("VideoDuration", VidzCommonMethods.getMediaDuration(context, uri))
                            startActivityForResult(intent, Constants.MAIN_VIDEO_TRIM)
                        }
                    }
                }
            } catch(e: Exception) {

            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == Activity.RESULT_CANCELED) return

        when(requestCode) {
            Constants.VIDEO_GALLERY -> {
                data?.let {
                    setFilePath(resultCode, it, Constants.VIDEO_GALLERY)
                }
            }

            Constants.RECORD_VIDEO -> {
                data?.let {
                    Log.v(tagName, "data: ${data.data}")

                    if(resultCode == RESULT_OK) {
                        masterVideoFile = VidzCommonMethods.writeIntoFile(activity, data, videoFile)

                        val timeInMillis = VidzUtils.getVideoDuration(context!!, masterVideoFile!!)
                        Log.v(tagName, "Time in Millis: $timeInMillis")

                        val duration = VidzCommonMethods.convertDurationInMinutes(timeInMillis)
                        Log.v(tagName, "Duration: $duration")

                        if(duration < Constants.VIDEO_LIMIT) {
                            playbackPosition = 0
                            currentWindow = 0
                            initializePlayer()
                        } else {
                            Toast.makeText(activity, getString(R.string.error_select_smaller_video), Toast.LENGTH_SHORT).show()

                            val uri = Uri.fromFile(masterVideoFile)
                            val intent = Intent(context, VidzTrimmerActivity::class.java)
                            intent.putExtra("VideoPath", masterVideoFile!!.absolutePath)
                            intent.putExtra("VideoDuration", VidzCommonMethods.getMediaDuration(context, uri))
                            startActivityForResult(intent, Constants.MAIN_VIDEO_TRIM)
                        }
                    }
                }
            }


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

                VidzVideoEditor.with(context!!)
                    .setType(Constants.CONVERT_AVI_TO_MP4)
                    .setFile(masterVideoFile!!)
                    .setOutputPath(outputFile.path)
                    .setCallback(this)
                    .main()

                showLoading(true)
            }
            .setNegativeButton(R.string.no) {
                dialog, which -> releasePlayer()
            }
            .show()
    }
    private fun releasePlayer() {
        if(exoplayer != null) {
            playbackPosition = exoplayer?.currentPosition!!
            currentWindow = exoplayer?.currentWindowIndex!!
            playWhenReady = exoplayer?.playWhenReady
            exoplayer?.release()
            exoplayer = null
        }
    }

    override fun onResume() {
        super.onResume()
        if(SDK_INT > 23 || exoplayer == null) {
            masterVideoFile?.let {
                if(!isLargeVideo!!) {
                    initializePlayer()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if(SDK_INT > 23) {
            masterVideoFile?.let {
                initializePlayer()
            }
        }
    }

}