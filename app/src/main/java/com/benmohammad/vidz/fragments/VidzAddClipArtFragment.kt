package com.benmohammad.vidz.fragments

import android.content.Context
import android.media.Image
import android.os.Bundle
import android.os.Environment
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
import com.benmohammad.vidz.adapter.VidzClipArtAdapter
import com.benmohammad.vidz.adapter.VidzPositionAdapter
import com.benmohammad.vidz.interfaces.VidzClipArtListener
import com.benmohammad.vidz.interfaces.VidzFFMpegCallback
import com.benmohammad.vidz.interfaces.VidzPositionListener
import com.benmohammad.vidz.utils.Constants
import com.benmohammad.vidz.utils.VidzUtils
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

import java.io.File

class VidzAddClipArtFragment : BottomSheetDialogFragment(), VidzClipArtListener, VidzPositionListener, VidzFFMpegCallback  {

    private var tagName: String = VidzAddClipArtFragment::class.java.simpleName
    private lateinit var rootView: View
    private lateinit var linearLayoutManagerOne: LinearLayoutManager
    private lateinit var linearLayoutManagerTwo: LinearLayoutManager
    private lateinit var rvClipArt: RecyclerView
    private lateinit var rvPosition: RecyclerView
    private lateinit var ivClose: ImageView
    private lateinit var ivDone: ImageView
    private var videoFile: File? = null
    private var clipArtFilePath: ArrayList<String> = ArrayList()
    private var positionList: ArrayList<String> = ArrayList()
    private var helper: VidzBaseCreatorDialogFragment.CallBacks? = null
    private lateinit var vidzClipArtAdapter: VidzClipArtAdapter
    private lateinit var vidzPositionAdapter: VidzPositionAdapter
    private var selectedPositionItem: String? = null
    private var selectedFilePath: String? = null
    private var mContext: Context? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.vidz_fragment_add_clipart, container, false)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvClipArt = rootView.findViewById(R.id.rvClipArt)
        rvPosition = rootView.findViewById(R.id.rvPosition)
        ivClose = rootView.findViewById(R.id.iv_close)
        ivDone = rootView.findViewById(R.id.iv_done)
        linearLayoutManagerOne = LinearLayoutManager(activity!!.applicationContext)
        linearLayoutManagerTwo = LinearLayoutManager(activity!!.applicationContext)
        linearLayoutManagerOne.orientation = LinearLayoutManager.HORIZONTAL
        rvClipArt.layoutManager = linearLayoutManagerOne
        linearLayoutManagerTwo.orientation = LinearLayoutManager.HORIZONTAL
        rvPosition.layoutManager = linearLayoutManagerTwo

        mContext = context
        val listFile: Array<File>
        val file = File(
            Environment.getExternalStorageDirectory(),
            File.separator + Constants.APP_NAME + File.separator + Constants.CLIP_ARTS + File.separator
        )

        if(file.isDirectory) {
            listFile = file.listFiles()
            for(i in listFile.indices) {
                clipArtFilePath.add(listFile[i].absolutePath)
            }
        }

        vidzClipArtAdapter = VidzClipArtAdapter(clipArtFilePath, activity!!.applicationContext, this)
        rvClipArt.adapter = vidzClipArtAdapter
        vidzClipArtAdapter.notifyDataSetChanged()

        positionList.add(Constants.BOTTOM_LEFT)
        positionList.add(Constants.BOTTOM_RIGHT)
        positionList.add(Constants.CENTRE)
        positionList.add(Constants.TOP_LEFT)
        positionList.add(Constants.TOP_RIGHT)

        vidzPositionAdapter = VidzPositionAdapter(positionList, activity!!.applicationContext, this)
        rvPosition.adapter = vidzPositionAdapter
        vidzPositionAdapter.notifyDataSetChanged()

        ivClose.setOnClickListener { dismiss() }

        ivDone.setOnClickListener {
            vidzClipArtAdapter.setClipArt()
            vidzPositionAdapter.setPosition()

            if(selectedFilePath != null) {
                if(selectedPositionItem != null) {
                    dismiss()

                    when(selectedPositionItem) {
                        Constants.BOTTOM_LEFT -> {
                            addClipArtAction(selectedFilePath!!, Constants.BOTTOM_LEFT)
                        }

                        Constants.BOTTOM_RIGHT -> {
                            addClipArtAction(selectedFilePath!!, Constants.BOTTOM_RIGHT)
                        }

                        Constants.CENTRE -> {
                            addClipArtAction(selectedFilePath!!, Constants.CENTRE)
                        }

                        Constants.TOP_LEFT -> {
                            addClipArtAction(selectedFilePath!!, Constants.TOP_LEFT)
                        }

                        Constants.TOP_RIGHT -> {
                            addClipArtAction(selectedFilePath!!, Constants.TOP_RIGHT)
                        }
                    }
                } else {
                    VidzUtils.showGlideToast(activity!!, getString(R.string.error_select_sticker_pos))
                }
            } else {
                VidzUtils.showGlideToast(activity!!, getString(R.string.error_select_sticker))
            }
        }
    }

    private fun addClipArtAction(imgPath: String, position: String) {
        val outputFile = VidzUtils.createVideoFile(context!!)
        Log.v(tagName, "outputFile : ${outputFile}")

        VidzVideoEditor.with(context!!)
            .setType(Constants.VIDEO_CLIP_ART_OVERLAY)
            .setFile(videoFile!!)
            .setOutputPath(outputFile.path)
            .setImagePath(imgPath)
            .setCallback(this)
            .main()

        helper?.showLoading(true)
    }

    fun setHelper(helper: VidzBaseCreatorDialogFragment.CallBacks) {
        this.helper = helper
    }

    fun setFilePathFromSource(file: File) {
        videoFile = file
    }



    override fun selectedClipArt(path: String) {
        selectedFilePath = path
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
        Toast.makeText(mContext, "video processing failed", Toast.LENGTH_SHORT).show()
        helper?.showLoading(false)
    }

    override fun onNotAvailable(error: Exception) {
        Log.v(tagName, "onNotAvailable()  ${error.localizedMessage}")
    }

    override fun onFinish() {
        Log.v(tagName, "onFinish()")
        helper?.showLoading(false)
    }
}