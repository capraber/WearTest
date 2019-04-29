package com.yalantis.watchface.service

import android.graphics.BitmapFactory
import android.util.Log
import com.google.android.gms.wearable.MessageEvent
import com.yalantis.watchface.events.WatchfaceUpdatedEvent
import de.greenrobot.event.EventBus

class WearableListenerService : com.google.android.gms.wearable.WearableListenerService() {

    override fun onMessageReceived(messageEvent: MessageEvent) {
        Log.i("WearableListenerService","messageReceived")

        val offset = 0
        val offset_path = "offset"

        if (messageEvent.path.contains(offset_path)) {
            EventBus.getDefault()
                .post(WatchfaceUpdatedEvent(messageEvent.path, Integer.parseInt(String(messageEvent.data))))
        } else {
            val message = messageEvent.data
            val bitmap = BitmapFactory.decodeByteArray(message, offset, message.size, null)
            EventBus.getDefault().post(WatchfaceUpdatedEvent(messageEvent.path, bitmap))
        }
    }
}
