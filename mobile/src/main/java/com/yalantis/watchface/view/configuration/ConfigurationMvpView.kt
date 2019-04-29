package com.yalantis.watchface.view.configuration

import android.content.Intent
import com.yalantis.watchface.view.MvpView

interface ConfigurationMvpView : MvpView {

    fun startFileChooser(intent: Intent, requestCode: Int)

    fun showStatusMessage(message: String)
}
