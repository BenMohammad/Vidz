package com.benmohammad.vidz.adapter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.benmohammad.vidz.R
import com.benmohammad.vidz.interfaces.VidzFilterListener
import com.facebook.drawee.view.SimpleDraweeView

class VidzFilterAdapter(filterList: ArrayList<String>, bitmap: Bitmap, val context: Context, vidzFilterListener: VidzFilterListener)
    : RecyclerView.Adapter<VidzFilterAdapter.MyViewHolder>()
{
    private var tagName: String = VidzFilterAdapter::class.java.simpleName
    private var myFilterList = filterList
    private var myBitmap = bitmap
    private var myFilterListener = vidzFilterListener
    private var selectedPosition: Int = -1
    private var selectedFilter: String? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(LayoutInflater.from(context).inflate(R.layout.vidz_filter_view, parent, false))
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.tvFilter.text = myFilterList[position]

        if(selectedPosition == position) {
            holder.clFilter.setBackgroundColor(Color.WHITE)
            holder.tvFilter.setTextColor(Color.BLACK)
        } else {
            holder.clFilter.setBackgroundColor(Color.BLACK)
            holder.tvFilter.setTextColor(Color.WHITE)
        }

        holder.ivFilter.setImageBitmap(myBitmap)
        holder.ivFilter.setOnClickListener {
            selectedPosition = position
            selectedFilter = myFilterList[holder.adapterPosition]
            notifyDataSetChanged()
        }
    }

    fun setFilter() {
        if(selectedFilter != null) {
            Log.v(tagName ,"selected filter: $selectedFilter")
            myFilterListener.selectedFilter(selectedFilter!!)
        }
    }



    class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        var tvFilter : TextView = itemView.findViewById(R.id.tvFilter)
        var ivFilter : SimpleDraweeView = itemView.findViewById(R.id.ivFilter)
        var clFilter : ConstraintLayout = itemView.findViewById(R.id.clFilter)
    }

    override fun getItemCount(): Int {
        return myFilterList.size
    }

}