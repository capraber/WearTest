package com.yalantis.watchface.task;

import android.graphics.Bitmap;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.io.ByteArrayOutputStream;

public class SendToDataLayerThread extends Thread {

    private static final int MAX_SIZE = 200000000;
    private static final String MESSAGE_SUCESS=" was sent successfully";
    private static final String FAIL_SIZE="Big image file, try to use another";
    private String path;
    private Bitmap bitmap;
    private GoogleApiClient mGoogleApiClient;
    private DataLayerListener mDataLayerListener;

    public SendToDataLayerThread(String path, Bitmap bitmap, GoogleApiClient googleApiClient, DataLayerListener dataLayerListener) {
        this.path = path;
        this.bitmap = bitmap;
        mGoogleApiClient = googleApiClient;
        mDataLayerListener = dataLayerListener;
    }

    public void run() {
        NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        String message = path + MESSAGE_SUCESS;
        if (bitmap.getByteCount() < MAX_SIZE) {
            for (Node node : nodes.getNodes()) {
                Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(), path,  stream.toByteArray()).await();
            }
        } else {
            message = FAIL_SIZE;
        }
        mDataLayerListener.onSuccess(message);
    }

    public interface DataLayerListener  {

        void onSuccess(String message);

    }

}
