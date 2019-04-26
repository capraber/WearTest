package com.yalantis.watchface.presenter.configuration;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.yalantis.watchface.App;
import com.yalantis.watchface.Constants;
import com.yalantis.watchface.R;
import com.yalantis.watchface.task.SendToDataLayerThread;
import com.yalantis.watchface.view.configuration.ConfigurationActivity;
import com.yalantis.watchface.view.configuration.ConfigurationMvpView;

import java.io.IOException;

import static android.content.Context.NOTIFICATION_SERVICE;

public class ConfigurationPresenterImpl implements ConfigurationPresenter, SendToDataLayerThread.DataLayerListener {

    private ConfigurationMvpView mConfigurationView;

    private final static String NOTIFICATION_TITLE = "Imagen actualizada";
    private final static String NOTIFICATION_TEXT = "Imagen actualizada coon exito en tu watchFace";
    public final static int NOTIFICATION_ID = 1903;

    @Override
    public void register(ConfigurationMvpView holder) {
        mConfigurationView = holder;
    }

    @Override
    public void unregister(ConfigurationMvpView holder) {
        mConfigurationView = null;
    }

    @Override
    public void onActivityResult(int resultCode, Intent data, int requestCode, GoogleApiClient googleApiClient) {
        try {
            if (resultCode == Activity.RESULT_OK) {
                Uri selectedImageUri = data.getData();
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(mConfigurationView.getContext().getContentResolver(), selectedImageUri);
                App.getConfigurationManager().updateField(requestCode, bitmap);
                new SendToDataLayerThread(Constants.resourceKeyMap.get(requestCode), bitmap, googleApiClient, this).start();
            }
        } catch (IOException e) {
            Log.e("TAG", e.getMessage());
        }
    }

    @Override
    public void changeContentImage(boolean isConnected, int type) {
        if (isConnected) {
            startFileChooser(type);
        }
    }

    private void startFileChooser(int fileSelectCode) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        mConfigurationView.startFileChooser(intent, fileSelectCode);
    }

    @Override
    public void saveConfig() {
        App.getConfigurationManager().saveConfiguration();
    }

    @Override
    public void sendNotification(ConfigurationActivity configurationActivity) {

        Intent intentAction = new Intent(configurationActivity, ConfigurationActivity.class);

       PendingIntent pendingIntent = PendingIntent.getActivity(configurationActivity, 1, intentAction, PendingIntent.FLAG_ONE_SHOT);

        NotificationManager notificationManager = (NotificationManager) configurationActivity.getSystemService(NOTIFICATION_SERVICE);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(configurationActivity);
        notificationBuilder.setSmallIcon(R.drawable.ic_launcher);
        notificationBuilder.setContentTitle(NOTIFICATION_TITLE);
        notificationBuilder.setContentText(NOTIFICATION_TEXT);
        notificationBuilder.setContentIntent(pendingIntent);
        notificationBuilder.setAutoCancel(true);

        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }


    @Override
    public void onSuccess(String message) {
        mConfigurationView.showStatusMessage(message);
    }
}
