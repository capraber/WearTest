package com.yalantis.watchface.view.configuration

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.view.View
import com.yalantis.watchface.BACKGROUND_CHOOSER
import com.yalantis.watchface.R
import com.yalantis.watchface.presenter.configuration.ConfigurationPresenter
import com.yalantis.watchface.presenter.configuration.ConfigurationPresenterImpl
import com.yalantis.watchface.view.BaseGoogleApiActivity
import kotlinx.android.synthetic.main.activity_configuration.*

class ConfigurationActivity : BaseGoogleApiActivity(), ConfigurationMvpView {

    private var mConfigurationPresenter: ConfigurationPresenter = ConfigurationPresenterImpl()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_configuration)

        setSupportActionBar(toolbar)
        mConfigurationPresenter.register(this)

        button_change_background.setOnClickListener { view ->
            onClickChangeBackground(view)
        }
        button_change_save_configuration.setOnClickListener { view ->
            onClickSaveConfig(view)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mConfigurationPresenter.unregister(this)
    }

    fun onClickChangeBackground(view: View) {
        mConfigurationPresenter.changeContentImage(isConnected, BACKGROUND_CHOOSER)
    }

    fun onClickSaveConfig(view: View) {
        mConfigurationPresenter.saveConfig()
        Snackbar.make(linear_layout_root, getString(R.string.saved_message), Snackbar.LENGTH_SHORT)
                .show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        mConfigurationPresenter.onActivityResult(resultCode, data, requestCode, mGoogleApiClient)
        mConfigurationPresenter.sendNotification(this)
    }

    override fun startFileChooser(intent: Intent, requestCode: Int) {
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_file)), requestCode)
    }

    override fun showStatusMessage(message: String) {
        Snackbar.make(findViewById(R.id.linear_layout_root), message, Snackbar.LENGTH_SHORT)
    }

    override fun getContext(): Context {
        return this
    }
}
