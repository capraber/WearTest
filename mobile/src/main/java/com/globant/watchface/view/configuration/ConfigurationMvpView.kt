package com.globant.watchface.view.configuration

import android.content.Intent
import com.globant.watchface.view.MvpView

interface ConfigurationMvpView : MvpView {

    fun startFileChooser(intent: Intent, requestCode: Int)

    fun showStatusMessage(message: String)
}
