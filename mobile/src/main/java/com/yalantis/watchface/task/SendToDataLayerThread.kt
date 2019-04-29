package com.yalantis.watchface.task

import android.graphics.Bitmap
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.wearable.Wearable
import java.io.ByteArrayOutputStream

class SendToDataLayerThread(
        private val path: String,
        private val bitmap: Bitmap,
        private val mGoogleApiClient: GoogleApiClient,
        private val mDataLayerListener: DataLayerListener
) : Thread() {

    companion object {
        private const val MAX_SIZE = 2000000
        private const val MESSAGE_ERROR = "Big image file, try to use another"
        private const val MESSAGE_OK = "was sent successfully"
        private const val QUALITY_IMG = 100
    }

    override fun run() {
        val nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await()
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, QUALITY_IMG, stream)
        var message = "$path $MESSAGE_OK"
        if (bitmap.byteCount < MAX_SIZE) {
            for (node in nodes.nodes) {
                Wearable.MessageApi.sendMessage(mGoogleApiClient, node.id, path, stream.toByteArray()).await()
            }
        } else {
            message = MESSAGE_ERROR
        }
        mDataLayerListener.onSuccess(message)
    }

    interface DataLayerListener {
        fun onSuccess(message: String)
    }
}
