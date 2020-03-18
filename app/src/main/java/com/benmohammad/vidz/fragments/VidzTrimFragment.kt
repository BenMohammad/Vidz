package com.benmohammad.vidz.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.widget.AppCompatTextView
import com.benmohammad.vidz.R
import com.benmohammad.vidz.VidzVideoEditor
import com.benmohammad.vidz.interfaces.VidzFFMpegCallback
import com.benmohammad.vidz.utils.Constants
import com.benmohammad.vidz.utils.VidzUtils
import com.benmohammad.vidz.utils.VidzVideoUtils
import com.github.guilhe.views.SeekBarRangedView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.io.File

class VidzTrimFragment: BottomSheetDialogFragment(), VidzFFMpegCallback {

    private var tagName: String = VidzTrimFragment::class.java.simpleName
    private lateinit var rootView: View
    private lateinit var ivClose: ImageView
    private lateinit var ivDone: ImageView
    private var videoFile: File? = null
    private var helper: VidzBaseCreatorDialogFragment.CallBacks? = null
    private var sbrvVideoTrim: SeekBarRangedView? = null
    private var actvStartTime: AppCompatTextView? = null
    private var actvEndTime: AppCompatTextView? = null
    private var duration: Long? = null
    private var mContext: Context? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.vidz_fragment_trim, container, false)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ivClose = rootView.findViewById(R.id.iv_close)
        ivDone = rootView.findViewById(R.id.iv_done)
        sbrvVideoTrim = rootView.findViewById(R.id.sbrvVideoTrim)
        actvStartTime = rootView.findViewById(R.id.actvStartTime)
        actvEndTime = rootView.findViewById(R.id.actvEndTime)

        mContext = context

        ivClose.setOnClickListener {
            dismiss()
        }

        ivDone.setOnClickListener {
            val outputFile = VidzUtils.createVideoFile(context!!)
            Log.v(tagName, "outputFile: ${outputFile.absolutePath}")

            VidzVideoEditor.with(context!!)
                .setType(Constants.VIDEO_TRIM)
                .setFile(videoFile!!)
                .setOutputPath(outputFile.path)
                .setStartTime(actvStartTime?.text.toString())
                .setEndTime(actvEndTime?.text.toString())
                .setCallback(this)
                .main()

            helper?.showLoading(true)
            dismiss()
        }
        Log.v(tagName, "duration: $duration")
        Log.v(tagName, "duration: " + VidzVideoUtils.secToTime(duration!!))

        sbrvVideoTrim?.minValue = 0f
        sbrvVideoTrim?.maxValue = duration?.toFloat()!!
        actvStartTime?.text = VidzVideoUtils.secToTime(0)
        actvEndTime?.text = VidzVideoUtils.secToTime(duration!!)

        sbrvVideoTrim?.setOnSeekBarRangedChangeListener(object: SeekBarRangedView.OnSeekBarRangedChangeListener{
            override fun onChanged(view: SeekBarRangedView?, minValue: Float, maxValue: Float) {

            }

            override fun onChanging(view: SeekBarRangedView?, minValue: Float, maxValue: Float) {
                Log.v(tagName, "minValue: $minValue maxValue $maxValue")
                actvStartTime?.text  = VidzVideoUtils.secToTime(minValue.toLong())
                actvEndTime?.text = VidzVideoUtils.secToTime(maxValue.toLong())
            }
        })
    }

    fun setHelper(helper: VidzBaseCreatorDialogFragment.CallBacks) {
        this.helper = helper
    }

    fun setFilePathFromSource(file: File, duration: Long) {
        videoFile = file
        this.duration = duration
    }

    override fun onProgress(progress: String) {
        Log.v(tagName, "onProgress() $progress")
        helper?.showLoading(false)
    }

    override fun onSuccess(convertedFile: File, type: String) {
        Log.v(tagName, "onSuccess()")
        helper?.showLoading(false)
        helper?.onFileProcessed(convertedFile)
    }

    override fun onFailure(error: Exception) {
        Log.d(tagName, "onFailure() ${error.localizedMessage}")
        Toast.makeText(mContext, "Video processing failed", Toast.LENGTH_SHORT).show()
        helper?.showLoading(false)
    }

    override fun onNotAvailable(error: Exception) {
        Log.d(tagName, "onNotAvailable() " + error.message)
        Log.v(tagName, "Excption ${error.localizedMessage}")
    }

    override fun onFinish() {
        Log.d(tagName,"onFinish()" )
        helper?.showLoading(false)
    }


}