package com.globant.watchface

import android.app.Application
import com.globant.watchface.manager.ConfigurationManager

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        configurationManager.init(this)
    }

    companion object {

        val configurationManager = ConfigurationManager()
    }
}
