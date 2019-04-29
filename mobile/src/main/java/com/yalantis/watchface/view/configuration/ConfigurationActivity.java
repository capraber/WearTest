package com.yalantis.watchface.view.configuration;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;

import com.yalantis.watchface.Constants;
import com.yalantis.watchface.R;
import com.yalantis.watchface.presenter.configuration.ConfigurationPresenter;
import com.yalantis.watchface.presenter.configuration.ConfigurationPresenterImpl;
import com.yalantis.watchface.view.BaseGoogleApiActivity;

public class ConfigurationActivity extends BaseGoogleApiActivity implements ConfigurationMvpView {

    protected ConfigurationPresenter mConfigurationPresenter = new ConfigurationPresenterImpl();
    Toolbar toolbar;
    LinearLayout linearLayoutRoot;
    NotificationCompat.Builder notificacion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuration);
        toolbar = findViewById(R.id.toolbar);
        linearLayoutRoot = findViewById(R.id.linear_layout_root);

        setSupportActionBar(toolbar);
        setTitle(getString(R.string.main_label));
        mConfigurationPresenter.register(this);
        notificacion = new NotificationCompat.Builder(this);
        notificacion.setAutoCancel(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mConfigurationPresenter.unregister(this);
    }

    public void onClickChangeBackground(View view) {
        mConfigurationPresenter.changeContentImage(isConnected, Constants.BACKGROUND_CHOOSER);
    }

    public void onClickSaveConfig(View view) {
        mConfigurationPresenter.saveConfig();
        Snackbar.make(linearLayoutRoot, getString(R.string.saved_message), Snackbar.LENGTH_SHORT)
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mConfigurationPresenter.onActivityResult(resultCode, data, requestCode, mGoogleApiClient);
        mConfigurationPresenter.sendNotification(this);
    }

    @Override
    public void startFileChooser(Intent intent, int requestCode) {
        startActivityForResult(
                Intent.createChooser(intent, getString(R.string.select_file)),
                requestCode);
    }

    @Override
    public void showStatusMessage(String message) {
        Snackbar.make(findViewById(R.id.linear_layout_root), message, Snackbar.LENGTH_SHORT);
    }

    @Override
    public Context getContext() {
        return this;
    }
}
