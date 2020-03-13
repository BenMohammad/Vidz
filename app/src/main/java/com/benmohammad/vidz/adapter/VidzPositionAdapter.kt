package com.benmohammad.vidz.adapter

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.benmohammad.vidz.R
import com.benmohammad.vidz.interfaces.VidzPositionListener

class VidzPositionAdapter(positionList: ArrayList<String>, val context: Context, vidzPositionListener: VidzPositionListener)
    : RecyclerView.Adapter<VidzPositionAdapter.MyViewHolder>()
{
    private var tagName: String = VidzPositionAdapter::class.java.simpleName
    private var myPositionList = positionList
    private var myPositionListener = vidzPositionListener
    private var selectedPosition: Int = -1
    private var selectedPositionItem: String? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.vidz_playback_view, parent, false))
    }

    override fun getItemCount(): Int {
        return myPositionList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.tvSpeed.text = myPositionList[position]

        if(selectedPosition == position) {
            holder.tvSpeed.setBackgroundColor(Color.WHITE)
            holder.tvSpeed.setTextColor(Color.BLACK)
        } else {
            holder.tvSpeed.setBackgroundColor(Color.BLACK)
            holder.tvSpeed.setTextColor(Color.WHITE)
        }

        holder.tvSpeed.setOnClickListener {

            selectedPosition = position
            selectedPositionItem = myPositionList[holder.adapterPosition]
            notifyDataSetChanged()
        }
    }

    fun setPosition() {
        if(selectedPosition != null) {
            Log.v(tagName, "selectedPositionitem: $selectedPositionItem")
            myPositionListener.selectedPosition(selectedPositionItem!!)
        }
    }




    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        var tvSpeed: TextView = itemView.findViewById(R.id.tv_speed)
    }
}