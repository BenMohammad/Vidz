package com.benmohammad.vidz.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.benmohammad.vidz.R
import com.benmohammad.vidz.VidzVideoEditor
import com.benmohammad.vidz.adapter.VidzPositionAdapter
import com.benmohammad.vidz.interfaces.VidzFFMpegCallback
import com.benmohammad.vidz.interfaces.VidzPositionListener
import com.benmohammad.vidz.utils.Constants
import com.benmohammad.vidz.utils.VidzUtils
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.io.File

class VidzAddTextFragment: BottomSheetDialogFragment(), VidzPositionListener, VidzFFMpegCallback {

    private var tagName: String = VidzAddTextFragment::class.java.simpleName
    private lateinit var rootView: View
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var rvPosition: RecyclerView
    private lateinit var ivClose : ImageView
    private lateinit var ivDone : ImageView
    private var videoFile: File? = null
    private var helper: VidzBaseCreatorDialogFragment.CallBacks? = null
    private lateinit var vidzPositionAdapter: VidzPositionAdapter
    private var positionList: ArrayList<String> = ArrayList()
    private var selectedPositionItem : String? = null
    private var etText: EditText? = null
    private var positionStr: String? = null
    private var mContext: Context? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.vidz_fragment_add_text, container, false)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvPosition = rootView.findViewById(R.id.rvPosition)
        ivClose = rootView.findViewById(R.id.iv_close)
        ivDone = rootView.findViewById(R.id.iv_done)
        etText = rootView.findViewById(R.id.etText)
        linearLayoutManager = LinearLayoutManager(activity!!.applicationContext)

        linearLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
        rvPosition.layoutManager = linearLayoutManager

        mContext = context
        ivClose.setOnClickListener { dismiss() }

        ivDone.setOnClickListener {
            val text = etText!!.text.toString().trim()
            Log.v(tagName, "userText $text")

            if(text.isNotEmpty()) {
                vidzPositionAdapter.setPosition()

                if(selectedPositionItem != null) {
                    dismiss()

                    when(selectedPositionItem) {
                        Constants.BOTTOM_LEFT -> {
                            positionStr = VidzVideoEditor.POSITION_BOTTOM_LEFT
                        }

                        Constants.BOTTOM_RIGHT -> {
                            positionStr = VidzVideoEditor.POSITION_BOTTOM_RIGHT
                        }

                        Constants.CENTRE_ALIGN -> {
                            positionStr = VidzVideoEditor.POSITION_CENTER_ALLIGN
                        }

                        Constants.CENTRE_BOTTOM -> {
                            positionStr = VidzVideoEditor.POSITION_CENTER_BOTTOM
                        }

                        Constants.TOP_LEFT -> {
                            positionStr = VidzVideoEditor.TOP_LEFT
                        }

                        Constants.TOP_RIGHT -> {
                            positionStr = VidzVideoEditor.POSITION_TOP_RIGHT
                        }
                    }

                    val outputFile = VidzUtils.createVideoFile(context!!)
                    Log.v(tagName, "outputFile ${outputFile.absolutePath}")




                VidzVideoEditor.with(context!!)
                    .setType(Constants.VIDEO_TEXT_OVERLAY)
                    .setFile(videoFile!!)
                    .setOutputPath(outputFile.path)
                    .setText(text)
                    .setColor("#fff")
                    .setSize("32")
                    .addBorder(false)
                    .setPosition(positionStr!!)
                    .setCallback(this)
                    .main()

                    helper?.showLoading(true)

            } else {
                VidzUtils.showGlideToast(activity!!, getString(R.string.error_add_text_pos))}
            }

            else {
                VidzUtils.showGlideToast(activity!!, getString(R.string.error_add_text))
            }
        }

        positionList.add(Constants.BOTTOM_RIGHT)
        positionList.add(Constants.CENTRE_ALIGN)
        positionList.add(Constants.CENTRE_BOTTOM)
        positionList.add(Constants.TOP_LEFT)
        positionList.add(Constants.TOP_RIGHT)

        vidzPositionAdapter = VidzPositionAdapter(positionList, activity!!.applicationContext, this)
        rvPosition.adapter = vidzPositionAdapter
        vidzPositionAdapter.notifyDataSetChanged()
    }

    fun setHelper(helper: VidzBaseCreatorDialogFragment.CallBacks) {
        this.helper = helper
    }

    fun setFilePathFromSource(file: File) {
        videoFile = file
    }

    override fun selectedPosition(position: String) {
        selectedPositionItem = position
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
        Log.v(tagName, "onFailure() ${error.localizedMessage}")
        Toast.makeText(mContext, "Video processing failed", Toast.LENGTH_SHORT).show()
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