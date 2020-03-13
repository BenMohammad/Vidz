package com.benmohammad.vidz.adapter

import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.benmohammad.vidz.R
import com.benmohammad.vidz.interfaces.VidzClipArtListener
import java.io.File

class VidzClipArtAdapter (clipArtList: ArrayList<String>, val context: Context, vidzClipArtListener: VidzClipArtListener)
    : RecyclerView.Adapter<VidzClipArtAdapter.MyViewHolder>()
{

    private var tagName: String = VidzClipArtAdapter::class.java.simpleName
    private var myClipArtList = clipArtList
    private var myClipArtListener = vidzClipArtListener
    private var selectedPosition: Int = -1
    private var selectedFilePath: String? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.vidz_clipart_view, parent, false))
    }


    override fun getItemCount(): Int {
        return myClipArtList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val uri = Uri.fromFile(File(myClipArtList[position]))
        holder.tvClipArt.setImageURI(uri)

        if(selectedPosition == position) {
            holder.tvClipArt.setBackgroundColor(Color.WHITE)
        } else {
            holder.tvClipArt.setBackgroundColor(Color.BLACK)
        }


        holder.tvClipArt.setOnClickListener {
            selectedPosition = position
            selectedFilePath = myClipArtList[holder.adapterPosition]
            notifyDataSetChanged()
        }
    }

    fun setClipArt() {
        if(selectedFilePath != null) {
            Log.v(tagName, "selectedFilePath: $selectedFilePath")
            myClipArtListener.selectedClipArt(selectedFilePath!!)
        }
    }



    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvClipArt: ImageView = itemView.findViewById(R.id.tv_clip_art)
    }
}