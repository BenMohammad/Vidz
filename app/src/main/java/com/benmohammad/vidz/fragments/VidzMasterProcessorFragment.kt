package com.benmohammad.vidz.fragments

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.benmohammad.vidz.R
import com.benmohammad.vidz.adapter.VidzVideoOptionsAdapter
import com.benmohammad.vidz.interfaces.VidzBaseCreatorDialogFragment
import com.benmohammad.vidz.interfaces.VidzFFMpegCallback
import com.benmohammad.vidz.interfaces.VidzOptionListener
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.PlayerView
import kotlinx.android.synthetic.main.vidz_layout_custom_exo_player.*
import java.io.File

class VidzMasterProcessorFragment : Fragment(), VidzBaseCreatorDialogFragment.CallBacks {

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

    private fun initView(rootView: View) {
        ePlayer = rootView?.findViewById(R.id.ePlayer)

    }

    override fun onDidNothing() {
        //initializePlayer()
    }

    override fun onFileProcessed(file: File) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getFile(): File? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun reInitPlayer() {
        //initializePlayer()
    }

    override fun onAudioFileProcessed(convertedAudioFile: File) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun showLoading(isShow: Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun openGallery() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun openCamera() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}