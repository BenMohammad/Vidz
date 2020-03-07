package com.benmohammad.vidz.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import com.jeevandeshmukh.glidetoastlib.GlideToast
import java.io.*
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

object VidzUtils {

    val outPath: String
    get() {
        val path = Environment.getExternalStorageDirectory().toString() + File.separator + Constants.APP_NAME + File.separator

        val folder = File(path)
        if(!folder.exists())
            folder.mkdirs()

        return path
    }

    fun copyFileToInternalStorage(resouceId: Int, resourceName: String, context: Context) : File {
        val path = Environment.getExternalStorageDirectory().toString() + File.separator + Constants.APP_NAME + File.separator + Constants.CLIP_ARTS + File.separator

        val folder = File(path)
        if(!folder.exists())
            folder.mkdirs()

        val dataPath = "$path$resourceName.png"
        Log.v("VidzUtils", "path:  $dataPath")
        try {
            val inputStream = context.resources.openRawResource(resouceId)
            inputStream.toFile(dataPath)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch(e: IOException) {
            e.printStackTrace()
        }

        return File(dataPath)
    }

    private fun InputStream.toFile(path: String) {
        File(path).outputStream().use { this.copyTo(it) }
    }

    fun getConvertedFile(folder: String, fileName: String) : File {
        val f =File(folder)

        if(!f.exists())
            f.mkdirs()

        return File(f.path + File.separator + fileName)
    }

    fun refreshGallery(path: String, context: Context) {
        val file = File(path)
        try {
            val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            val contentUri = Uri.fromFile(file)
            mediaScanIntent.data = contentUri
            context.sendBroadcast(mediaScanIntent)
        } catch(e: Exception) {
            e.printStackTrace()
        }
    }

    fun refreshGalleryAlone(context: Context) {
        try {
            val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            context.sendBroadcast(mediaScanIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun isVideoHaveAudioTrack(path: String): Boolean {
        var audioTrack = false
        var retriever = MediaMetadataRetriever()
        val hasAudioStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_AUDIO)
        audioTrack = hasAudioStr == "yes"

        return audioTrack
    }

    fun showGlideToast(activity: Activity, content: String) {
        GlideToast.makeToast(
            activity,
            content,
            GlideToast.LENGTHLONG,
            GlideToast.FAILTOAST,
            GlideToast.TOP
        ).show()
    }

    fun createVideoFile(context: Context) : File {
        val timeStamp: String = SimpleDateFormat(Constants.DATE_FORMAT, Locale.getDefault()).format(Date())
        val imageFileName: String = Constants.APP_NAME + timeStamp+ "_"
        val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES)
        if(!storageDir!!.exists()) storageDir.mkdirs()
        return File.createTempFile(imageFileName, Constants.AUDIO_FORMAT, storageDir)
    }

    fun createAudioFile(context: Context) : File {
        val timeStamp: String = SimpleDateFormat(Constants.DATE_FORMAT, Locale.getDefault()).format(Date())
        val imageFileName: String = Constants.APP_NAME + timeStamp + "_"
        val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES)
        if(!storageDir!!.exists()) storageDir.mkdirs()
        return File.createTempFile(imageFileName, Constants.AUDIO_FORMAT, storageDir)
    }

    fun getVideoDuration(context: Context, file: File) : Long {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(context, Uri.fromFile(file))
        val time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        val timeInMillis = time.toLong()
        retriever.release()
        return timeInMillis
    }
}