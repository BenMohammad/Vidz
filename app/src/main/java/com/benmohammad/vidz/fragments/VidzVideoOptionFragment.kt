package com.benmohammad.vidz.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import com.benmohammad.vidz.R
import com.benmohammad.vidz.interfaces.VidzBaseCreatorDialogFragment
import com.benmohammad.vidz.interfaces.VidzDialogHelper
import java.io.File

class VidzVideoOptionFragment : VidzBaseCreatorDialogFragment(), VidzDialogHelper {

    private var acivClose: AppCompatImageView? = null
    private var tvGallery: TextView? = null
    private var tvCamera: TextView? = null
    private var helper: CallBacks? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return super.onCreateView(inflater, container, savedInstanceState)

        val inflate = inflater.inflate(R.layout.vidz_add_video_includer, container, false)
        initView(inflate)
        return inflate
    }

    private fun initView(inflate: View?) {
        acivClose = inflate?.findViewById(R.id.acivClose)
        tvGallery = inflate?.findViewById(R.id.tvGallery)
        tvCamera = inflate?.findViewById(R.id.tvCamera)

        acivClose?.setOnClickListener {
            dialog?.dismiss()
        }

        tvGallery?.setOnClickListener{
            dialog?.dismiss()
            helper?.openGallery()
        }

        tvCamera?.setOnClickListener {
            dialog?.dismiss()
            helper?.openCamera()
        }
    }

    override fun setHelper(helper: CallBacks) {
        this.helper = helper;
    }

    override fun setMode(mode: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setFilePathFromSource(file: File) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setDuration(duration: Long) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun permissionBlocked() {

    }

    companion object {
        fun newInstance() = VidzVideoOptionFragment()
    }

}