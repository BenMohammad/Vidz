package com.benmohammad.vidz.fragments

import android.app.Activity
import android.content.ContentResolver
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.media.audiofx.BassBoost
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.DialogFragment
import com.benmohammad.vidz.R
import com.github.hiteshsondhi88.libffmpeg.FFmpeg
import java.io.File
import java.util.jar.Manifest

abstract class VidzBaseCreatorDialogFragment : DialogFragment() {

    private var permissionRequired = arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.share_dialog1)

        this.isCancelable = false
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode) {
            130 -> {
                for (permission in permissions) {
                    if(ActivityCompat.shouldShowRequestPermissionRationale(activity as Activity, permission)) {
                        Toast.makeText(context, "Permission Denied", Toast.LENGTH_SHORT).show()
                    } else {
                        if(ActivityCompat.checkSelfPermission(context!!, permissionRequired[0])== PackageManager.PERMISSION_GRANTED) {
                            //save image
                        } else {
                            callPermissionsSettings()
                        }
                    }
                }
                return
            }
        }
    }


    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        stopRunningProcess()
    }

    private fun callPermissionsSettings() {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        val uri = Uri.fromParts("package", context!!.applicationContext.packageName, null)
        intent.data = uri
        startActivityForResult(intent, 300)
    }

    override fun onResume() {
        super.onResume()

        if(ActivityCompat.checkSelfPermission(context!!, permissionRequired[0]) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(permissionRequired, 130)
        }
    }

    interface CallBacks {
        fun onDidNothing()
        fun onFileProcessed(file: File)
        fun getFile(): File?
        fun reInitPlayer()
        fun onAudioFileProcessed(convertedAudioFile: File)
        fun showLoading(isShow: Boolean)
        fun openGallery()
        fun openCamera()
    }

    abstract fun permissionBlocked()

    fun stopRunningProcess() {
        FFmpeg.getInstance(activity).killRunningProcesses()
    }

    fun isRunning(): Boolean {
        return FFmpeg.getInstance(activity).isFFmpegCommandRunning
    }

    fun showInProgressToast() {
        Toast.makeText(activity, "Operation in progress!!", Toast.LENGTH_SHORT).show()
    }

    fun getMimeType(url: String) : String? {
        var type: String? = null
        val extension = MimeTypeMap.getFileExtensionFromUrl(url)
        if(extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        }

        return type
    }

}