package com.benmohammad.vidz.fragments

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.benmohammad.vidz.R
import com.benmohammad.vidz.VidzVideoEditor
import com.benmohammad.vidz.interfaces.VidzDialogHelper
import com.benmohammad.vidz.interfaces.VidzFFMpegCallback
import com.benmohammad.vidz.utils.Constants
import com.benmohammad.vidz.utils.VidzUtils
import com.facebook.drawee.view.SimpleDraweeView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.io.File

class VidzMergeFragment: BottomSheetDialogFragment(), VidzDialogHelper, VidzFFMpegCallback {

    private var tagName: String = VidzMergeFragment::class.java.simpleName
    private lateinit var rootView: View
    private lateinit var ivClose: ImageView
    private lateinit var ivDone: ImageView
    private lateinit var ivVideoOne: SimpleDraweeView
    private lateinit var ivVideoTwo: SimpleDraweeView
    private var videoFileOne: File? = null
    private var videoFileTwo: File? = null
    private var bmThumnailOne: Bitmap? = null
    private var bmThumnailTwo: Bitmap? = null
    private var helper: VidzBaseCreatorDialogFragment.CallBacks? = null
    private var mContext: Context? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.vidz_fragment_merge_dialog, container, false)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ivClose = rootView.findViewById(R.id.iv_close)
        ivDone = rootView.findViewById(R.id.iv_done)
        ivVideoOne = rootView.findViewById(R.id.iv_video_one)
        ivVideoTwo = rootView.findViewById(R.id.iv_video_two)

        mContext = context

        ivClose.setOnClickListener {
            dismiss()
        }

        ivDone.setOnClickListener {
            if (videoFileOne != null && videoFileTwo != null) {
                dismiss()


                val outputFile = VidzUtils.createVideoFile(context!!)
                Log.v(tagName, "outputFile: ${outputFile.absolutePath}")

                VidzVideoEditor.with(context!!)
                    .setType(Constants.MERGE_VIDEO)
                    .setFile(videoFileOne!!)
                    .setFileTwo(videoFileTwo!!)
                    .setOutputPath(outputFile.path)
                    .setCallback(this)
                    .main()

                helper?.showLoading(true)
            } else {
                VidzUtils.showGlideToast(activity!!, getString(R.string.error_merge))
            }
        }

        ivVideoOne.setOnClickListener {
            checkPermission(Constants.VIDEO_MERGE_1, Manifest.permission.READ_EXTERNAL_STORAGE)
        }


        ivVideoTwo.setOnClickListener {
            checkPermission(Constants.VIDEO_MERGE_1, Manifest.permission.READ_EXTERNAL_STORAGE)
        }

    }


    override fun setHelper(helper: VidzBaseCreatorDialogFragment.CallBacks) {
        this.helper = helper
    }

    override fun setMode(mode: Int) {
    }

    override fun setFilePathFromSource(file: File) {
    }

    override fun setDuration(duration: Long) {
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
            Constants.VIDEO_MERGE_1 -> {
                for(permission in permissions) {
                    if(ActivityCompat.shouldShowRequestPermissionRationale(activity as Activity, permission)) {
                        Toast.makeText(activity, "Permission denied", Toast.LENGTH_SHORT).show()
                    } else {
                        if(ActivityCompat.checkSelfPermission(activity as Activity, permission) == PackageManager.PERMISSION_GRANTED) {
                            val i = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
                            i.setType("video/*")
                            i.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("audio/*", "video/*"))
                            startActivityForResult(i, Constants.VIDEO_MERGE_1)
                        } else {
                            callPermissionSettings()
                        }
                    }
                }
                return
            }
            Constants.VIDEO_MERGE_2 -> {
                for(permission in permissions) {
                    if(ActivityCompat.shouldShowRequestPermissionRationale(activity as Activity, permission)) {
                        Toast.makeText(activity, "Permission denied", Toast.LENGTH_SHORT).show()
                    } else {
                        if(ActivityCompat.checkSelfPermission(activity as Activity, permission) == PackageManager.PERMISSION_GRANTED) {
                            val i = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
                            i.setType("video/*")
                            i.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("audio/*", "video/*"))
                            startActivityForResult(i, Constants.VIDEO_MERGE_2)
                        } else {
                            callPermissionSettings()
                        }
                    }
                }

                return
            }
        }
    }

    private fun callPermissionSettings() {
        val intent = Intent()
        intent.action  = Settings   .ACTION_APPLICATION_DETAILS_SETTINGS
        val uri = Uri.fromParts("package", context!!.applicationContext.packageName, null)
        intent.data = uri
        startActivityForResult(intent, 300)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_CANCELED) return

        when(requestCode) {
            Constants.VIDEO_MERGE_1 -> {
                data?.let {
                    setFilePath(resultCode, it, Constants.VIDEO_MERGE_1)
                }
            }

            Constants.VIDEO_MERGE_2 -> {
                data?.let {
                    setFilePath(resultCode, it, Constants.VIDEO_MERGE_2)
                }
            }
        }
    }

    private fun setFilePath(resultCode: Int, data: Intent, mode: Int) {
        if(resultCode == Activity.RESULT_OK) {
            try {
                val selectedImage = data.data
                val filePathColumn = arrayOf(MediaStore.MediaColumns.DATA)
                val cursor = context!!.contentResolver
                    .query(selectedImage!!, filePathColumn, null, null, null)

                if(cursor != null) {
                    cursor.moveToFirst()
                    val columnIndex = cursor.getColumnIndex(filePathColumn[0])
                    val filePath = cursor.getString(columnIndex)
                    cursor.close()
                    if(mode == Constants.VIDEO_MERGE_1) {
                        videoFileOne = File(filePath)
                        Log.v(tagName, "videoFileOne " + videoFileOne!!.absolutePath)

                        bmThumnailOne = ThumbnailUtils.createVideoThumbnail(
                            videoFileOne!!.absolutePath,
                            MediaStore.Images.Thumbnails.FULL_SCREEN_KIND
                        )

                        ivVideoOne.setImageBitmap(bmThumnailOne)
                    } else if(mode == Constants.VIDEO_MERGE_2) {
                        videoFileTwo = File(filePath)
                        Log.v(tagName, "videoFileTwo: " + videoFileTwo!!.absolutePath )
                        bmThumnailTwo = ThumbnailUtils.createVideoThumbnail(
                            videoFileTwo!!.absolutePath,
                            MediaStore.Images.Thumbnails.FULL_SCREEN_KIND
                        )

                        ivVideoTwo.setImageBitmap(bmThumnailTwo)
                    }
                }
            } catch(e: Exception) {
                Log.e(tagName, "Exception: ${e.localizedMessage}")
            }
        }
    }

    companion object {
        fun newInstance() = VidzMergeFragment()
    }

    override fun onProgress(progress: String) {
        Log.v(tagName, "onProgress()")
    }

    override fun onSuccess(convertedFile: File, type: String) {
        Log.v(tagName, "onSuccess()")
        helper?.showLoading(false)
        helper?.onFileProcessed(convertedFile)
    }

    override fun onFailure(error: Exception) {
        Log.v(tagName, "onFailure(): ${error.localizedMessage}")
        Toast.makeText(mContext, "Video processing failed", Toast.LENGTH_SHORT).show()
        helper?.showLoading(false)
    }

    override fun onNotAvailable(error: Exception) {
        Log.v(tagName, "onNotAvailable(): ${error.localizedMessage}")
    }

    override fun onFinish() {
        Log.v(tagName, "onFinish()")
        helper?.showLoading(false)
    }
}