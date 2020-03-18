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
import com.benmohammad.vidz.adapter.VidzTransitionAdapter
import com.benmohammad.vidz.interfaces.VidzFFMpegCallback
import com.benmohammad.vidz.interfaces.VidzFilterListener
import com.benmohammad.vidz.utils.Constants
import com.benmohammad.vidz.utils.VidzUtils
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.io.File

class VidzTransitionFragment: BottomSheetDialogFragment(), VidzFilterListener, VidzFFMpegCallback {

    private var tagName: String = VidzTransitionFragment::class.java.simpleName
    private lateinit var rootView: View
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var rvTransition: RecyclerView
    private lateinit var ivClose: ImageView
    private lateinit var ivDone: ImageView
    private var videoFile: File? = null
    private var helper: VidzBaseCreatorDialogFragment.CallBacks? = null
    private var transitionsList: ArrayList<String> = ArrayList()
    private lateinit var adapter: VidzTransitionAdapter
    private var selectedTransition: String? = null
    private var mContext: Context? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.vidz_fragment_transition, container, false)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rvTransition = rootView.findViewById(R.id.rvTransition)
        ivClose = rootView.findViewById(R.id.iv_close)
        ivDone = rootView.findViewById(R.id.iv_done)
        linearLayoutManager = LinearLayoutManager(activity!!.applicationContext)

        mContext = context
        ivClose.setOnClickListener {
            dismiss()
        }

        ivDone.setOnClickListener {

            adapter.setTransition()

            if (selectedTransition != null) {
                dismiss()


                when (selectedTransition) {
                    "Fade in/out" -> {
                        applyTransitionAction()
                    }
                }
            }
        }
        linearLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
        rvTransition.layoutManager = linearLayoutManager

        transitionsList.add("Fade in/out")
        adapter = VidzTransitionAdapter(transitionsList, activity!!.applicationContext, this)
        rvTransition.adapter = adapter
        adapter.notifyDataSetChanged()
    }

    private fun applyTransitionAction() {
        val outputFile = VidzUtils.createVideoFile(context!!)
        Log.v(tagName, "outputFile: ${outputFile.absolutePath}")

        VidzVideoEditor.with(context!!)
            .setType(Constants.VIDEO_TRANSITION)
            .setFile(videoFile!!)
            .setOutputPath(outputFile.path)
            .setCallback(this)
            .main()

        helper?.showLoading(true)
    }

    override fun selectedFilter(filter: String) {
        selectedTransition = filter
    }

    fun setHelper(helper: VidzBaseCreatorDialogFragment.CallBacks) {
        this.helper = helper
    }

    fun setFilePathFromSource(file: File) {
        videoFile = file
    }

    override fun onProgress(progress: String) {
        Log.v(tagName, "onProgress $progress")
    }

    override fun onSuccess(convertedFile: File, type: String) {
        Log.v(tagName, "onSuccess() ")
        helper?.showLoading(false)
        helper?.onFileProcessed(convertedFile)
    }

    override fun onFailure(error: Exception) {
        Log.v(tagName, "onFailure() ${error.localizedMessage}")
        Toast.makeText(mContext, "Video processing failure", Toast.LENGTH_SHORT).show()
        helper?.showLoading(false)
    }

    override fun onNotAvailable(error: Exception) {
        Log.v(tagName, "onNotAvailable() ${error.localizedMessage}")
    }

    override fun onFinish() {
        Log.v(tagName, "onFinish()")
        helper?.showLoading(false)
    }
}