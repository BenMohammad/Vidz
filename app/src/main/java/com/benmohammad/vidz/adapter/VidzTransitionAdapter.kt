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
import com.benmohammad.vidz.interfaces.VidzFilterListener

class VidzTransitionAdapter(transitionList: ArrayList<String>, val context: Context, vidzFilterListener: VidzFilterListener )
    : RecyclerView.Adapter<VidzTransitionAdapter.MyViewHolder>()
{

    private var tagName: String = VidzTransitionAdapter::class.java.simpleName
    private var myTransitionsList = transitionList
    private var myFilterListener = vidzFilterListener
    private var selectedPosition: Int = -1
    private var selectedTransition: String? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(LayoutInflater.from(context).inflate(R.layout.vidz_playback_view, parent, false))
    }


    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.tvSpeed.text =  myTransitionsList[position]

        if(selectedPosition == position) {
            holder.tvSpeed.setBackgroundColor(Color.WHITE)
            holder.tvSpeed.setTextColor(Color.BLACK)
        } else {
            holder.tvSpeed.setBackgroundColor(Color.BLACK)
            holder.tvSpeed.setTextColor(Color.WHITE)
        }

        holder.tvSpeed.setOnClickListener {
            selectedPosition = position
            selectedTransition = myTransitionsList[holder.adapterPosition]
            notifyDataSetChanged()
        }
    }

    fun setTransition() {
        if(selectedPosition != null) {
            Log.v(tagName, "selectedTransition: $selectedTransition")
            myFilterListener.selectedFilter(selectedTransition!!)
        }
    }

    class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        var tvSpeed: TextView = itemView.findViewById(R.id.tv_speed)
    }

    override fun getItemCount(): Int {
        return myTransitionsList.size
    }
}