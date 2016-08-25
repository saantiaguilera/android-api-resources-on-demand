package com.u.dynamic_resources.internal;

import android.graphics.Bitmap;
import android.net.Uri;

import com.u.dynamic_resources.core.Configurations;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by saguilera on 8/25/16.
 */
public class Pipeline {

    private static Pipeline instance;

    private OkHttpClient client;

    public static Pipeline getInstance() {
        if (instance == null) {
            synchronized (Pipeline.class) {
                if (instance == null) {
                    instance = new Pipeline();
                }
            }
        }

        return instance;
    }

    private Pipeline() {}

    public void apply(Configurations configurations) {
        //TODO
    }

    public File fetch(Uri uri) {

    }

}
