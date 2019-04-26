package com.yalantis.watchface.service

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.yalantis.watchface.*
import java.util.HashMap

class InitialWatchFaceService : AbstractAnalogWatchFaceService() {
    private val map = HashMap<String, Bitmap>()

    override fun getContext(): Context {
        return this
    }

    override fun getBitmaps(): MutableMap<String, Bitmap> {
        val resources = getContext().resources
        map[BG_BUNDLE_FLAG] = BitmapFactory.decodeResource(resources, R.drawable.bg)
        map[BG_AMBIENT_BUNDLE_FLAG] = BitmapFactory.decodeResource(resources, R.drawable.bg_ambient)
        map[SEC_TICK_BUNDLE_FLAG] = BitmapFactory.decodeResource(resources, R.drawable.tick)
        map[MIN_TICK_BUNDLE_FLAG] = BitmapFactory.decodeResource(resources, R.drawable.min)
        map[HR_TICK_BUNDLE_FLAG] = BitmapFactory.decodeResource(resources, R.drawable.hrs)
        map[HR_TICK_AMBIENT_BUNDLE_FLAG] = BitmapFactory.decodeResource(resources, R.drawable.hrs_ambient)
        map[MIN_TICK_AMBIENT_BUNDLE_FLAG] = BitmapFactory.decodeResource(resources, R.drawable.min_ambient)
        return map
    }
}
