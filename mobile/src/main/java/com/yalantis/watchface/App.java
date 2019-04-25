package com.yalantis.watchface;

import android.app.Application;

import com.yalantis.watchface.manager.ConfigurationManager;

public class App extends Application{

    private static ConfigurationManager configurationManager = new ConfigurationManager();

    @Override
    public void onCreate() {
        super.onCreate();
        configurationManager.init(this);
    }

    public static ConfigurationManager getConfigurationManager() {
        return configurationManager;
    }
}
