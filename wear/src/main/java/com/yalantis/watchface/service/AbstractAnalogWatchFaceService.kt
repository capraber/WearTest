package com.yalantis.watchface.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.*
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.wearable.watchface.CanvasWatchFaceService
import android.support.wearable.watchface.WatchFaceStyle
import android.util.Log
import android.view.SurfaceHolder
import com.yalantis.watchface.*
import com.yalantis.watchface.events.WatchfaceUpdatedEvent
import de.greenrobot.event.EventBus
import java.util.*
import java.util.concurrent.TimeUnit

abstract class AbstractAnalogWatchFaceService : CanvasWatchFaceService() {

    abstract fun getContext(): Context

    protected abstract fun getBitmaps(): MutableMap<String, Bitmap>

    override fun onCreateEngine(): CanvasWatchFaceService.Engine {
        return Engine()
    }

    companion object {

        private const val ORIGINAL_SIZE = 320f

        private const val STROKE_WIDTH = 5f
        private const val HOUR_STROKE_WIDTH = 15f
        private const val MINUTE_STROKE_WIDTH = 10f
        private const val SECOND_TICK_STROKE_WIDTH = 5f

        private const val SHADOW_RADIUS = 6

        private const val MSG_UPDATE_TIME = 0
        private val INTERACTIVE_UPDATE_RATE_MS = TimeUnit.SECONDS.toMillis(1)

        private const val MINUTES_TO_DEGREES = 360 / 60
        private const val HOURS_TO_DEGREES = 360 / 12

        private const val THREESIXTY_DEGREES = 360f
        private const val SCALE_VALUE = 2f

        private const val ZERO_FLOAT = 0f

        private const val SECONDS = 60f
        private const val MINUTES = 60f
        private const val MILLISECOND_VALUE = 1000f
    }

    /**
     * Manages the US flag watch face behaviour.
     */
    protected inner class Engine : CanvasWatchFaceService.Engine() {

        private var k = 1f

        private var secondOffset = 0
        private var hoursOffset = 0
        private var minuteOffset = 0

        private lateinit var wfBitmap: Bitmap
        private lateinit var secBitmap: Bitmap
        private lateinit var secScaledBitmap: Bitmap
        private lateinit var hrBitmap: Bitmap
        private lateinit var hrScaledBitmap: Bitmap
        private lateinit var minBitmap: Bitmap
        private lateinit var minScaledBitmap: Bitmap

        private lateinit var wfAmbientBitmap: Bitmap
        private lateinit var hrAmbientBitmap: Bitmap
        private lateinit var hrAmbientScaledBitmap: Bitmap
        private lateinit var minAmbientBitmap: Bitmap
        private lateinit var minAmbientScaledBitmap: Bitmap

        private var registerReceiverFlag = false

        private var centerX = 0f
        private var centerY = 0f

        private lateinit var calendar: Calendar
        private lateinit var bundle: MutableMap<String, Bitmap>
        /**
         * Handled to update time each second in interactive mode.
         */
        private val updateTimeHandler = object : Handler() {
            override fun handleMessage(message: Message) {
                super.handleMessage(message)
                when (message.what) {
                    MSG_UPDATE_TIME -> {
                        invalidate()
                        if (shouldTimerBeRunning()) {
                            val timeMs = System.currentTimeMillis()
                            val delayMs = INTERACTIVE_UPDATE_RATE_MS - timeMs % INTERACTIVE_UPDATE_RATE_MS
                            this.sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs)
                        }
                    }
                }
            }
        }

        /**
         * Called when time zone changes in app runtime.
         */
        private val timeZoneReceiver: BroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                calendar.timeZone = TimeZone.getDefault()
                invalidate()
            }
        }

        private lateinit var mHandPaint: Paint
        override fun onCreate(holder: SurfaceHolder) {

            val mHandPaintAntiAlias = true
            val showSystemTimer = false

            super.onCreate(holder)
            EventBus.getDefault().register(this)
            bundle = getBitmaps()
            initBitmaps()
            mHandPaint = Paint()
            mHandPaint.color = Color.CYAN
            mHandPaint.setShadowLayer(SHADOW_RADIUS.toFloat(), ZERO_FLOAT, ZERO_FLOAT, Color.BLACK)
            mHandPaint.strokeWidth = STROKE_WIDTH
            mHandPaint.isAntiAlias = mHandPaintAntiAlias
            mHandPaint.strokeCap = Paint.Cap.ROUND
            calendar = Calendar.getInstance()

            setWatchFaceStyle(WatchFaceStyle.Builder(this@AbstractAnalogWatchFaceService)
                    .setCardPeekMode(WatchFaceStyle.PEEK_MODE_SHORT)
                    .setBackgroundVisibility(WatchFaceStyle.BACKGROUND_VISIBILITY_INTERRUPTIVE)
                    .setShowSystemUiTime(showSystemTimer)
                    .build())
        }

        private fun initBitmaps() {
            bundle[BG_BUNDLE_FLAG]?.let { wfBitmap = it }
            bundle[MIN_TICK_BUNDLE_FLAG]?.let { minBitmap = it }
            bundle[HR_TICK_BUNDLE_FLAG]?.let { hrBitmap = it }
            bundle[SEC_TICK_BUNDLE_FLAG]?.let { secBitmap = it }

            //Init bitmaps for ambient mode.
            bundle[BG_AMBIENT_BUNDLE_FLAG]?.let { wfAmbientBitmap = it }
            bundle[MIN_TICK_AMBIENT_BUNDLE_FLAG]?.let { minAmbientBitmap = it }
            bundle[HR_TICK_AMBIENT_BUNDLE_FLAG]?.let { hrAmbientBitmap = it }
        }

        override fun onPropertiesChanged(properties: Bundle) {
            super.onPropertiesChanged(properties)
        }

        /**
         * Updates screen each minute.
         */
        override fun onTimeTick() {
            super.onTimeTick()
            invalidate()
        }

        fun onEvent(event: WatchfaceUpdatedEvent) {
            Log.i("AAWFS", "event")
            bundle[event.getResourceKey()] = event.getResourceBitmap()
            initBitmaps()
            invalidate()
        }

        private fun initOffset(key: String, offset: Int) {
            when {
                key.contains("seconds") -> secondOffset = offset
                key.contains("hours") -> hoursOffset = offset
                else -> minuteOffset = offset
            }
        }

        /**
         * Device goes into ambient or interactive mode.
         *
         * @param inAmbientMode true if device is in ambient mode after mode change.
         */
        override fun onAmbientModeChanged(inAmbientMode: Boolean) {
            super.onAmbientModeChanged(inAmbientMode)
            invalidate()
            updateTimer()
        }

        /**
         * Called within invalidate() to update (redraw) screen.
         *
         * @param canvas Main Canvas instance where the watch face in drawn.
         * @param bounds Physical bounds of the device.
         */
        override fun onDraw(canvas: Canvas, bounds: Rect) {
            val width = bounds.width()
            val height = bounds.height()
            val scaledBitmapFilter = true
            val filterBitmap = true

            calendar.timeInMillis = System.currentTimeMillis()
            //Scale bitmaps to expected sizes.
            wfBitmap = Bitmap.createScaledBitmap(wfBitmap, width, height, scaledBitmapFilter)
            wfAmbientBitmap = Bitmap.createScaledBitmap(wfAmbientBitmap, width, height, scaledBitmapFilter)

            if (height < ORIGINAL_SIZE) {
                k = height.toFloat() / ORIGINAL_SIZE
                secScaledBitmap = scaleBitmap(secBitmap)
                minScaledBitmap = scaleBitmap(minBitmap)
                hrScaledBitmap = scaleBitmap(hrBitmap)
                hrAmbientScaledBitmap = scaleBitmap(hrAmbientBitmap)
                minAmbientScaledBitmap = scaleBitmap(minAmbientBitmap)
            } else {
                secScaledBitmap = secBitmap
                minScaledBitmap = minBitmap
                hrScaledBitmap = hrBitmap
                hrAmbientScaledBitmap = hrAmbientBitmap
                minAmbientScaledBitmap = minAmbientBitmap
            }
            centerX = width / SCALE_VALUE
            centerY = height / SCALE_VALUE
            //Compute time units to display.
            val seconds = calendar.get(Calendar.SECOND) + calendar.get(Calendar.MILLISECOND) / MILLISECOND_VALUE
            val minutes = calendar.get(Calendar.MINUTE) + seconds / SECONDS
            val hours = calendar.get(Calendar.HOUR) + minutes / MINUTES

            //Calculate ticks rotation angle;
            val minutesRotation = minutes * MINUTES_TO_DEGREES + THREESIXTY_DEGREES
            val hoursRotation = hours * HOURS_TO_DEGREES + THREESIXTY_DEGREES

            //Is device in interactive mode?
            val isInteractive = !isInAmbientMode
            val mFilterPaint = Paint()
            mFilterPaint.isFilterBitmap = filterBitmap

            if (isInteractive) {
                //In interactive mode
                canvas.drawColor(Color.BLACK)
                canvas.drawBitmap(wfBitmap, ZERO_FLOAT, ZERO_FLOAT, mFilterPaint)
                drawBaseTicks(canvas, minutesRotation, hoursRotation, seconds)
            } else {
                //In ambient mode
                canvas.drawBitmap(wfAmbientBitmap, ZERO_FLOAT, ZERO_FLOAT, mFilterPaint)
                drawAmbientTicks(canvas, minutesRotation, hoursRotation)
            }
        }

        /**
         * Scales tick bitmap for given device.
         *
         * @param originalBitmap Original Bitmap instance to be source of scaling.
         * @return Scaled for given device Bitmap.
         */
        private fun scaleBitmap(originalBitmap: Bitmap): Bitmap {
            return Bitmap.createScaledBitmap(originalBitmap, (originalBitmap.width * k).toInt(), (originalBitmap.height * k).toInt(), true)
        }

        /**
         * Called when device does into or out of interactive mode.
         *
         * @param visible true if the device is in interactive mode after mode change.
         */
        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)
            if (visible) {
                registerReceiver()
                calendar.timeZone = TimeZone.getDefault()
            } else {
                unregisterReceiver()
            }
            updateTimer()
        }

        override fun onDestroy() {
            super.onDestroy()
            EventBus.getDefault().unregister(this)
        }

        /**
         * Updates watch if it's necessary more often then once a minute.
         */

        private fun updateTimer() {
            updateTimeHandler.removeMessages(MSG_UPDATE_TIME)
            if (shouldTimerBeRunning()) {
                updateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME)
            }
        }

        /**
         * @return true if device is in interactive mode.
         */
        private fun shouldTimerBeRunning(): Boolean {
            return isVisible && !isInAmbientMode
        }

        /**
         * Register time zone change BroadcastReceiver instance.
         */
        private fun registerReceiver() {
            if (registerReceiverFlag) {
                return
            }
            registerReceiverFlag = true
            val intentFilter = IntentFilter(Intent.ACTION_TIMEZONE_CHANGED)
            this@AbstractAnalogWatchFaceService.registerReceiver(timeZoneReceiver, intentFilter)
        }

        /**
         * Unregister time zone change BroadcastReceiver instance.
         */
        private fun unregisterReceiver() {
            if (!registerReceiverFlag) {
                return
            }
            registerReceiverFlag = false
            this@AbstractAnalogWatchFaceService.unregisterReceiver(timeZoneReceiver)
        }

        /**
         * Draws watch ticks in interactive mode.
         *
         * @param canvas          Canvas instance to draw ticks on.
         * @param minutesRotation Angle of minute tick rotation.
         * @param hoursRotation   Angle of hour tick rotation.
         * @param seconds         Number of past seconds in current minute.
         */
        private fun drawBaseTicks(canvas: Canvas, minutesRotation: Float, hoursRotation: Float, seconds: Float) {
            val mFilterPaint = Paint()
            mFilterPaint.isFilterBitmap = true
            mFilterPaint.isAntiAlias = true

            //Draw main ticks.
            canvas.save()
            canvas.rotate(minutesRotation, centerX, centerY)
            canvas.drawBitmap(minScaledBitmap, centerX - minScaledBitmap.width / SCALE_VALUE,
                    centerY - minScaledBitmap.height + minuteOffset * k, mFilterPaint)
            canvas.restore()


            canvas.save()
            canvas.rotate(hoursRotation, centerX, centerY)
            canvas.drawBitmap(hrScaledBitmap, centerX - hrScaledBitmap.width / SCALE_VALUE,
                    centerY - hrScaledBitmap.height + hoursOffset * k, mFilterPaint)

            canvas.restore()

            //Show second tick in interactive mode only.
            val secondsRotation = seconds * MINUTES_TO_DEGREES + THREESIXTY_DEGREES
            canvas.save()
            canvas.rotate(secondsRotation, centerX, centerY)
            canvas.drawBitmap(secScaledBitmap, centerX - secScaledBitmap.width / SCALE_VALUE,
                    centerY - secScaledBitmap.height + secondOffset * k, mFilterPaint)

            canvas.restore()
        }

        /**
         * Draws ticks in ambient mode.
         *
         * @param canvas          Canvas instance on draw ticks on.
         * @param minutesRotation Angle of minute tick rotation.
         * @param hoursRotation   Angle of hour tick rotation.
         */
        private fun drawAmbientTicks(canvas: Canvas, minutesRotation: Float, hoursRotation: Float) {
            val mFilterPaint = Paint()
            mFilterPaint.isFilterBitmap = true
            mFilterPaint.isAntiAlias = false
            val scale_modifier = 10

            //Draw main ticks.
            canvas.save()
            canvas.rotate(minutesRotation, centerX, centerY)
            canvas.drawBitmap(minAmbientScaledBitmap, centerX - minAmbientScaledBitmap.width / SCALE_VALUE,
                    centerY - minAmbientScaledBitmap.height + scale_modifier * k, mFilterPaint)
            canvas.restore()


            canvas.save()
            canvas.rotate(hoursRotation, centerX, centerY)
            canvas.drawBitmap(hrAmbientScaledBitmap, centerX - hrAmbientScaledBitmap.width / SCALE_VALUE,
                    centerY - hrAmbientScaledBitmap.height + scale_modifier * k, mFilterPaint)

            canvas.restore()

        }

    }
}

