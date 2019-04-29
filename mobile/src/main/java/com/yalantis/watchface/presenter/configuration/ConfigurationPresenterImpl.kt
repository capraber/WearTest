package com.yalantis.watchface.presenter.configuration

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.graphics.Bitmap
import android.provider.MediaStore
import android.support.v4.app.NotificationCompat
import android.util.Log
import com.google.android.gms.common.api.GoogleApiClient
import com.yalantis.watchface.App
import com.yalantis.watchface.Constants
import com.yalantis.watchface.R
import com.yalantis.watchface.task.SendToDataLayerThread
import com.yalantis.watchface.view.configuration.ConfigurationActivity
import com.yalantis.watchface.view.configuration.ConfigurationMvpView
import java.io.IOException

class ConfigurationPresenterImpl : ConfigurationPresenter, SendToDataLayerThread.DataLayerListener {

    companion object {

        private const val NOTIFICATION_TITLE = "Imagen actualizada"
        private const val NOTIFICATION_TEXT = "Imagen actualizada coon exito en tu watchFace"
        private const val PATH_IMAGE = "image/*"
        private const val NOTIFICATION_CHANNEL = "channel_name"
        private const val CHANNEL_DESCRIPTION = "channel_description"
        private const val NOTIFICATION_ID = 1903
        private const val TAG = "TAG"
        private const val REQUEST_CODE = 1
    }

    private lateinit var mConfigurationView: ConfigurationMvpView

    private lateinit var bitmap: Bitmap

    override fun register(holder: ConfigurationMvpView) {
        mConfigurationView = holder
    }

    override fun unregister(holder: ConfigurationMvpView) {
        mConfigurationView = ConfigurationActivity()
    }

    override fun onActivityResult(resultCode: Int, data: Intent, requestCode: Int, googleApiClient: GoogleApiClient) {
        try {
            if (resultCode == Activity.RESULT_OK) {
                val selectedImageUri = data.data
                bitmap = MediaStore.Images.Media.getBitmap(mConfigurationView.getContext().contentResolver, selectedImageUri)
                App.configurationManager.updateField(requestCode, bitmap)
                Constants.resourceKeyMap[requestCode]?.let { SendToDataLayerThread(it, bitmap, googleApiClient, this).start() }
            }
        } catch (e: IOException) {
            Log.e(TAG, e.message)
        }

    }

    override fun changeContentImage(isConnected: Boolean, type: Int) {
        if (isConnected) {
            startFileChooser(type)
        }
    }

    private fun startFileChooser(fileSelectCode: Int) {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = PATH_IMAGE
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        mConfigurationView.startFileChooser(intent, fileSelectCode)
    }

    override fun saveConfig() {
        App.configurationManager.saveConfiguration()
    }

    override fun sendNotification(configurationActivity: ConfigurationActivity) {

        val intentAction = Intent(configurationActivity, ConfigurationActivity::class.java)

        val pendingIntent = PendingIntent.getActivity(configurationActivity, REQUEST_CODE, intentAction, PendingIntent.FLAG_ONE_SHOT)

        val notificationManager = configurationActivity.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(NOTIFICATION_CHANNEL, CHANNEL_DESCRIPTION, NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }
        val notificationBuilder = NotificationCompat.Builder(configurationActivity)
        notificationBuilder.setSmallIcon(R.drawable.ic_launcher)
        notificationBuilder.setContentTitle(NOTIFICATION_TITLE)
        notificationBuilder.setContentText(NOTIFICATION_TEXT)
        notificationBuilder.setContentIntent(pendingIntent)
        notificationBuilder.setAutoCancel(true)

        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }

    override fun onSuccess(message: String) {
        mConfigurationView.showStatusMessage(message)
    }
}
