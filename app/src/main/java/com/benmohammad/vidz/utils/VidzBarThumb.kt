package com.benmohammad.vidz.utils

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.benmohammad.vidz.R
import kotlinx.android.synthetic.main.vidz_view_time_line.view.*
import java.util.*

class VidzBarThumb private constructor(){

    var index: Int = 0
    private set
    var `val` : Float = 0.toFloat()
    var pos: Float = 0.toFloat()
    var bitmap: Bitmap? = null
    private set(bitmap) {
        field = bitmap
        widthBitmap = bitmap?.width ?: 24
        heightBitmap = bitmap?.height ?: 24
    }

    var widthBitmap : Int = 0
    private set
    private var heightBitmap: Int = 0

    var lastTouchX: Float = 0.toFloat()

    init{

        `val` = 0f
        pos = 0f

    }

    companion object{
        val LEFT = 0
        val RIGHT = 0

        fun initThumbs(resources: Resources): List<VidzBarThumb> {
            val barThumbs = Vector<VidzBarThumb>()

            for(i in 0..1) {
                val th = VidzBarThumb()
                th.index = i
                if(i == 0) {
                    val resImageLeft = R.drawable.ic_video_cutline
                    th.bitmap = (BitmapFactory.decodeResource(resources, resImageLeft))
                    } else {
                        val resImageRight = R.drawable.ic_video_cutline
                        th.bitmap = (BitmapFactory.decodeResource(resources, resImageRight))
                }
                barThumbs.add(th)
            }
            return barThumbs
        }

        fun getWidthBitmap(barThumbs: List<VidzBarThumb>) : Int {
            return barThumbs[0].widthBitmap
        }

        fun getHeightBitmap(barThumbs: List<VidzBarThumb>) : Int {
            return barThumbs[0].heightBitmap
        }
            }

        }
