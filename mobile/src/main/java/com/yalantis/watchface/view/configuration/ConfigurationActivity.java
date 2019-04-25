package com.yalantis.watchface.view.configuration;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.widget.LinearLayout;

import com.yalantis.watchface.Constants;
import com.yalantis.watchface.R;
import com.yalantis.watchface.presenter.configuration.ConfigurationPresenter;
import com.yalantis.watchface.presenter.configuration.ConfigurationPresenterImpl;
import com.yalantis.watchface.view.BaseGoogleApiActivity;
//import com.yalantis.watchface.view.ticks_options.TickSetupActivity;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ConfigurationActivity extends BaseGoogleApiActivity implements ConfigurationMvpView {

    protected ConfigurationPresenter mConfigurationPresenter = new ConfigurationPresenterImpl();

    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.linear_layout_root)
    LinearLayout linearLayoutRoot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuration);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        setTitle(getString(R.string.main_label));
        mConfigurationPresenter.register(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mConfigurationPresenter.unregister(this);
    }

    @OnClick(R.id.button_change_background)
    void onClickChangeBackground() {
        mConfigurationPresenter.changeContentImage(isConnected, Constants.BACKGROUND_CHOOSER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mConfigurationPresenter.onActivityResult(resultCode, data, requestCode, mGoogleApiClient);
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
