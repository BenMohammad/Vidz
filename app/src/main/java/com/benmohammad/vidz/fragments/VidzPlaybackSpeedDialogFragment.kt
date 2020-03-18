package com.benmohammad.vidz.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.benmohammad.vidz.R
import com.benmohammad.vidz.VidzVideoEditor
import com.benmohammad.vidz.adapter.VidzPlaybackSpeedAdapter
import com.benmohammad.vidz.interfaces.VidzDialogHelper
import com.benmohammad.vidz.interfaces.VidzFFMpegCallback
import com.benmohammad.vidz.interfaces.VidzPlaybackSpeedListener
import com.benmohammad.vidz.utils.Constants
import com.benmohammad.vidz.utils.VidzUtils
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.io.File

class VidzPlaybackSpeedDialogFragment: BottomSheetDialogFragment(), VidzDialogHelper, VidzPlaybackSpeedListener, VidzFFMpegCallback {

    private var tagName: String = VidzPlaybackSpeedDialogFragment::class.java.simpleName
    private lateinit var rootView: View
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var rvPlaybackSpeed: RecyclerView
    private lateinit var playbackSpeedAdapter: VidzPlaybackSpeedAdapter
    private var playbackSpeed: ArrayList<String> = ArrayList()
    private lateinit var ivClose : ImageView
    private lateinit var ivDone : ImageView
    private var masterFile: File? = null
    private var isHavingAud = true
    private var helper: VidzBaseCreatorDialogFragment.CallBacks? = null
    private var mContext: Context? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.vidz_fragment_playback_speed_dialog, container, false)

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvPlaybackSpeed = rootView.findViewById(R.id.rvPlaybackSpeed)
        ivClose = rootView.findViewById(R.id.iv_close)
        ivDone = rootView.findViewById(R.id.iv_done)
        linearLayoutManager = LinearLayoutManager(activity!!.applicationContext)

        mContext = context

        ivClose.setOnClickListener {
            dismiss()
        }

        ivDone.setOnClickListener {
            playbackSpeedAdapter.setPlayBack()
        }

        linearLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
        rvPlaybackSpeed.layoutManager = linearLayoutManager

        playbackSpeed.add(Constants.SPEED_0_25)
        playbackSpeed.add(Constants.SPEED_0_5)
        playbackSpeed.add(Constants.SPEED_0_75)
        playbackSpeed.add(Constants.SPEED_1_0)
        playbackSpeed.add(Constants.SPEED_1_25)
        playbackSpeed.add(Constants.SPEED_1_5)

        playbackSpeedAdapter = VidzPlaybackSpeedAdapter(playbackSpeed, activity!!.applicationContext, this)
        rvPlaybackSpeed.adapter = playbackSpeedAdapter
        playbackSpeedAdapter.notifyDataSetChanged()

    }


    companion object {
        fun newInstance() = VidzPlaybackSpeedDialogFragment()
    }

    override fun setHelper(helper: VidzBaseCreatorDialogFragment.CallBacks) {
        this.helper = helper
    }

    override fun setMode(mode: Int) {

    }

    override fun setFilePathFromSource(file: File) {
        masterFile = file
        isHavingAud = VidzUtils.isVideoHaveAudioTrack(file.absolutePath)
        Log.d(tagName, "isHavingAudio: $isHavingAud")
    }

    override fun setDuration(duration: Long) {

    }

    override fun processVideo(playbackSpeed: String, tempo: String) {
        if(playbackSpeed != "0.0f") {
            val outputFile = VidzUtils.createVideoFile(context!!)
            Log.v(tagName, "outputFile: ${outputFile.absolutePath}")

            VidzVideoEditor.with(context!!)
                .setType(Constants.VIDEO_PLAYBACK_SPEED)
                .setFile(masterFile!!)
                .setOutputPath(outputFile.path)
                .setIsHavingAudio(isHavingAud)
                .setSpeedTempo(playbackSpeed, tempo)
                .setCallback(this)
                .main()

            helper?.showLoading(true)
            dismiss()
        } else {
            VidzUtils.showGlideToast(activity!!, getString(R.string.error_select_speed))
        }
    }

    override fun onProgress(progress: String) {
        Log.d(tagName, "onProgress() $progress")
    }

    override fun onSuccess(convertedFile: File, type: String) {
        Log.d(tagName, "onSuccess()")
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
        helper?.showLoading(false)
    }

    override fun onFinish() {
        Log.d(tagName, "onFinish()")
        helper?.showLoading(false)
    }
}