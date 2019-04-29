package com.yalantis.watchface

import android.app.Application
import com.yalantis.watchface.manager.ConfigurationManager

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        configurationManager.init(this)
    }

    companion object {

        val configurationManager = ConfigurationManager()
    }
}
