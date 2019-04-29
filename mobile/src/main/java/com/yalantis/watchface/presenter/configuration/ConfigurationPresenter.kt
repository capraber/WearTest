package com.yalantis.watchface.presenter.configuration

import android.content.Intent
import com.google.android.gms.common.api.GoogleApiClient
import com.yalantis.watchface.presenter.Presenter
import com.yalantis.watchface.view.configuration.ConfigurationMvpView
import com.yalantis.watchface.view.configuration.ConfigurationActivity



interface ConfigurationPresenter : Presenter<ConfigurationMvpView> {

    fun onActivityResult(resultCode: Int, data: Intent, requestCode: Int, googleApiClient: GoogleApiClient)

    fun changeContentImage(isConnected: Boolean, type: Int)

    fun saveConfig()

    fun sendNotification(configurationActivity: ConfigurationActivity)

}
