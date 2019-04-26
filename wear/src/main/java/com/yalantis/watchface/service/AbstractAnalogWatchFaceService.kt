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

    /**
     * Manages the US flag watch face behaviour.
     */
    protected inner class Engine : CanvasWatchFaceService.Engine() {

        private val MSG_UPDATE_TIME = 0
        private val INTERACTIVE_UPDATE_RATE_MS = TimeUnit.SECONDS.toMillis(1)

        private val MINUTES_TO_DEGREES = 360 / 60
        private val HOURS_TO_DEGREES = 360 / 12
        private var k = 1f

        private var secondOffset: Int = 0
        private var hoursOffset: Int = 0
        private var minuteOffset: Int = 0

        private lateinit var wfBitmap: Bitmap
        private lateinit var secBitmap: Bitmap
        private lateinit var secScaledBitmap: Bitmap
        private lateinit var minBitmap: Bitmap
        private lateinit var minScaledBitmap: Bitmap
        private lateinit var hrBitmap: Bitmap
        private lateinit var hrScaledBitmap: Bitmap
        private lateinit var wfAmbientBitmap: Bitmap
        private lateinit var hrAmbientBitmap: Bitmap
        private lateinit var hrAmbientScaledBitmap: Bitmap
        private lateinit var minAmbientBitmap: Bitmap
        private lateinit var minAmbientScaledBitmap: Bitmap

        private var registerReceiverFlag: Boolean = false

        private var centerX: Float = 0.toFloat()
        private var centerY: Float = 0.toFloat()

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
                calendar!!.timeZone = TimeZone.getDefault()
                invalidate()
            }
        }

        private lateinit var mHandPaint: Paint
        override fun onCreate(holder: SurfaceHolder) {
            super.onCreate(holder)
            EventBus.getDefault().register(this)
            bundle = getBitmaps()
            initBitmaps()
            mHandPaint = Paint()
            mHandPaint.color = Color.CYAN
            mHandPaint.setShadowLayer(SHADOW_RADIUS.toFloat(), 0f, 0f, Color.BLACK)
            mHandPaint.strokeWidth = STROKE_WIDTH
            mHandPaint.isAntiAlias = true
            mHandPaint.strokeCap = Paint.Cap.ROUND
            calendar = Calendar.getInstance()

            setWatchFaceStyle(WatchFaceStyle.Builder(this@AbstractAnalogWatchFaceService)
                    .setCardPeekMode(WatchFaceStyle.PEEK_MODE_SHORT)
                    .setBackgroundVisibility(WatchFaceStyle.BACKGROUND_VISIBILITY_INTERRUPTIVE)
                    .setShowSystemUiTime(false)
                    .build())
        }

        private fun initBitmaps() {
            wfBitmap = bundle[BG_BUNDLE_FLAG]!!
            minBitmap = bundle[MIN_TICK_BUNDLE_FLAG]!!
            hrBitmap = bundle[HR_TICK_BUNDLE_FLAG]!!
            secBitmap = bundle[SEC_TICK_BUNDLE_FLAG]!!

            //Init bitmaps for ambient mode.
            wfAmbientBitmap = bundle[BG_AMBIENT_BUNDLE_FLAG]!!
            minAmbientBitmap = bundle[MIN_TICK_AMBIENT_BUNDLE_FLAG]!!
            hrAmbientBitmap = bundle[HR_TICK_AMBIENT_BUNDLE_FLAG]!!
        }

        override fun onPropertiesChanged(properties: Bundle?) {
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
            if (event.getResourceBitmap() != null) {
                bundle!![event.getResourceKey()] = event.getResourceBitmap()
                initBitmaps()
            } else {
                initOffset(event.getResourceKey(), event.getOffset())
            }
            invalidate()
        }

        private fun initOffset(key: String, offset: Int) {
            if (key.contains("seconds")) {
                secondOffset = offset
            } else if (key.contains("hours")) {
                hoursOffset = offset
            } else {
                minuteOffset = offset
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
        override fun onDraw(canvas: Canvas?, bounds: Rect?) {
            val width = bounds!!.width()
            val height = bounds.height()

            calendar!!.timeInMillis = System.currentTimeMillis()
            //Scale bitmaps to expected sizes.
            wfBitmap = Bitmap.createScaledBitmap(wfBitmap!!, width, height, true)
            wfAmbientBitmap = Bitmap.createScaledBitmap(wfAmbientBitmap!!, width, height, true)
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
            centerX = width / 2f
            centerY = height / 2f
            //Compute time units to display.
            val seconds = calendar!!.get(Calendar.SECOND) + calendar!!.get(Calendar.MILLISECOND) / 1000f
            val minutes = calendar!!.get(Calendar.MINUTE) + seconds / 60f
            val hours = calendar!!.get(Calendar.HOUR) + minutes / 60f
            //Calculate ticks rotation angle;
            val minutesRotation = minutes * MINUTES_TO_DEGREES + 360f
            val hoursRotation = hours * HOURS_TO_DEGREES + 360f
            //Is device in interactive mode?
            val isInteractive = !isInAmbientMode
            val mFilterPaint = Paint()
            mFilterPaint.isFilterBitmap = true

            if (isInteractive) {
                //In interactive mode
                canvas!!.drawColor(Color.BLACK)
                canvas.drawBitmap(wfBitmap!!, 0f, 0f, mFilterPaint)
                drawBaseTicks(canvas, minutesRotation, hoursRotation, seconds)
            } else {
                //In ambient mode
                canvas!!.drawBitmap(wfAmbientBitmap!!, 0f, 0f, mFilterPaint)
                drawAmbientTicks(canvas, minutesRotation, hoursRotation)
            }
        }

        /**
         * Scales tick bitmap for given device.
         *
         * @param originalBitmap Original Bitmap instance to be source of scaling.
         * @return Scaled for given device Bitmap.
         */
        private fun scaleBitmap(originalBitmap: Bitmap?): Bitmap {
            return Bitmap.createScaledBitmap(originalBitmap!!, (originalBitmap.width * k).toInt(), (originalBitmap.height * k).toInt(), true)
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
                calendar!!.timeZone = TimeZone.getDefault()
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
            canvas.drawBitmap(minScaledBitmap!!, centerX - minScaledBitmap!!.width / 2f,
                    centerY - minScaledBitmap!!.height + minuteOffset * k, mFilterPaint)
            canvas.restore()


            canvas.save()
            canvas.rotate(hoursRotation, centerX, centerY)
            canvas.drawBitmap(hrScaledBitmap!!, centerX - hrScaledBitmap!!.width / 2f,
                    centerY - hrScaledBitmap!!.height + hoursOffset * k, mFilterPaint)

            canvas.restore()

            //Show second tick in interactive mode only.
            val secondsRotation = seconds * MINUTES_TO_DEGREES + 360f
            canvas.save()
            canvas.rotate(secondsRotation, centerX, centerY)
            canvas.drawBitmap(secScaledBitmap!!, centerX - secScaledBitmap!!.width / 2f,
                    centerY - secScaledBitmap!!.height + secondOffset * k, mFilterPaint)

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

            //Draw main ticks.
            canvas.save()
            canvas.rotate(minutesRotation, centerX, centerY)
            canvas.drawBitmap(minAmbientScaledBitmap!!, centerX - minAmbientScaledBitmap!!.width / 2f,
                    centerY - minAmbientScaledBitmap!!.height + 10 * k, mFilterPaint)
            canvas.restore()


            canvas.save()
            canvas.rotate(hoursRotation, centerX, centerY)
            canvas.drawBitmap(hrAmbientScaledBitmap!!, centerX - hrAmbientScaledBitmap!!.width / 2f,
                    centerY - hrAmbientScaledBitmap!!.height + 10 * k, mFilterPaint)

            canvas.restore()

        }

        private val STROKE_WIDTH = 5f
        private val HOUR_STROKE_WIDTH = 15f
        private val MINUTE_STROKE_WIDTH = 10f
        private val SECOND_TICK_STROKE_WIDTH = 5f

        private val SHADOW_RADIUS = 6

    }

    companion object {

        private val ORIGINAL_SIZE = 320f
    }
}

