package com.memory.memorypicturematch.utils

import android.graphics.Bitmap

object BitmapScaler {
    //scale and maintain aspect ratio given desire width
    //ButmapScaler.ScalToFitWidth(bitmap,100)
    fun scaleToFitWidth(b: Bitmap, width:Int): Bitmap {
        val factor = width / b.width.toFloat()
        return Bitmap.createScaledBitmap(b,width,(b.height*factor).toInt(),true)
    }
    //scale and maintain aspect ratio given desire height
    fun scaleToFitHeight(b: Bitmap, height: Int): Bitmap {
        val factor = height / b.height.toFloat()
        return Bitmap.createScaledBitmap(b,(b.width * factor).toInt(),height,true)
    }
}