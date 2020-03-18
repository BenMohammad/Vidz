package com.benmohammad.vidz.adapter

import android.content.Context
import android.graphics.Color
import android.text.Layout
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.benmohammad.vidz.R
import com.benmohammad.vidz.interfaces.VidzPlaybackSpeedListener
import com.benmohammad.vidz.utils.Constants
import kotlinx.android.synthetic.main.vidz_playback_view.view.*

class VidzPlaybackSpeedAdapter(private val playbackList: ArrayList<String>, val context: Context, vidzPlaybackSpeedListener: VidzPlaybackSpeedListener)
    : RecyclerView.Adapter<VidzPlaybackSpeedAdapter.MyViewHolder>()
{
    private var tagName: String = VidzPlaybackSpeedAdapter::class.java.simpleName
    private var myPlaybackList = playbackList
    private var myPlaybackListener = vidzPlaybackSpeedListener
    private var selectedPosition: Int =  -1
    private var selectedPlayback: Float = 0f
    private var selectedTempo: Float = 0f

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(LayoutInflater.from(context).inflate(R.layout.vidz_playback_view, parent, false))
    }

    override fun getItemCount(): Int {
        return myPlaybackList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.tvSpeed.text = myPlaybackList[position]

        if(selectedPosition == position) {
            holder.tvSpeed.setBackgroundColor(Color.WHITE)
            holder.tvSpeed.setTextColor(Color.BLACK)
        } else {
            holder.tvSpeed.setBackgroundColor(Color.BLACK)
            holder.tvSpeed.setTextColor(Color.WHITE)
        }

        holder.tvSpeed.setOnClickListener {
            selectedPosition = position

            when(playbackList[position]) {
                Constants.SPEED_0_25 -> {
                    selectedPlayback = 1.75f
                    selectedTempo = 0.5f
                }

                Constants.SPEED_0_5 -> {
                    selectedPlayback = 1.50f
                    selectedTempo = 0.5f
                }

                Constants.SPEED_0_75 -> {
                    selectedPlayback = 1.25f
                    selectedTempo = 0.75f
                }

                Constants.SPEED_1_0 -> {
                    selectedPlayback = 1.0f
                    selectedTempo = 1.0f
                }

                Constants.SPEED_1_25 -> {
                    selectedPlayback = 0.75f
                    selectedTempo = 1.25f
                }

                Constants.SPEED_1_5 -> {
                    selectedPlayback = 0.5f
                    selectedTempo = 2.0f
                }
            }

            notifyDataSetChanged()
        }
    }



    fun setPlayBack() {
        Log.v(tagName, "selectedPlayback: ${selectedPlayback.toString()}, selectedTempo ${selectedTempo.toString()  }")
        myPlaybackListener.processVideo(selectedPlayback.toString(), selectedTempo.toString())
    }


    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvSpeed: TextView = itemView.findViewById(R.id.tv_speed)
    }
}