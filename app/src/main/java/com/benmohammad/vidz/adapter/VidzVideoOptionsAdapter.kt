package com.benmohammad.vidz.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.benmohammad.vidz.R
import com.benmohammad.vidz.utils.Constants

class VidzVideoOptionsAdapter(videoOptions: ArrayList<String>, val context: Context, vidzVideoOptionListener : VidzVideoOptionsListener, orientationLand: Boolean )
    : RecyclerView.Adapter<VidzVideoOptionsAdapter.VidzPostViewHolder>() {

    private var videoOptions = videoOptions
    private var vidzVideoOptionsListener = vidzVideoOptionListener
    var orientationLand = orientationLand



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VidzPostViewHolder {
        return if(orientationLand) {
            VidzPostViewHolder(LayoutInflater.from(context).inflate(R.layout.vidz_option_view_land, parent, false))
        } else {
            VidzPostViewHolder(LayoutInflater.from(context).inflate(R.layout.vidz_option_view, parent, false))
        }
    }

    override fun onBindViewHolder(holder: VidzPostViewHolder, position: Int) {
        when(videoOptions[position]) {
            Constants.FLIRT -> {
                holder.ivOption.setImageResource(R.drawable.video_conf)
            }

            Constants.TRIM -> {
                holder.ivOption.setImageResource(R.drawable.video_trim)
            }

            Constants.MUSIC -> {
                holder.ivOption.setImageResource(R.drawable.music)
            }

            Constants.PLAYBACK -> {
            holder.ivOption.setImageResource(R.drawable.play)
            }

            Constants.TEXT -> {
                holder.ivOption.setImageResource(R.drawable.text)
            }

            Constants.OBJECT -> {
                holder.ivOption.setImageResource(R.drawable.sticker)
            }

            Constants.MERGE -> {
                holder.ivOption.setImageResource(R.drawable.merge)
            }

            Constants.TRANSITION -> {
                holder.ivOption.setImageResource(R.drawable.transition)
            }
        }

        holder.ivOption.setOnClickListener {
            vidzVideoOptionsListener.videoOption(videoOptions[position])
        }
    }

    class VidzPostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var ivOption: ImageView = itemView.findViewById(R.id.iv_option)
    }

    override fun getItemCount(): Int {
        return videoOptions.size
    }


}