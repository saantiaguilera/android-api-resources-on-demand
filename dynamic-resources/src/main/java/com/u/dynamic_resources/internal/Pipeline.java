package com.u.dynamic_resources.internal;

import com.u.dynamic_resources.core.Configurations;
import com.u.dynamic_resources.core.Pomu;

import okhttp3.OkHttpClient;

/**
 * Created by saguilera on 8/25/16.
 */
public class Pipeline {

    private static Pipeline instance;

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

    @SuppressWarnings("ConstantConditions")
    public void fetch(Request request) {
        Streamer.Builder builder = Streamer.with(request.getContext());

        if (Pomu.getConfigurations() != null && Pomu.getConfigurations().getClient() != null) {
            builder.client(Pomu.getConfigurations().getClient());
        }

        if (request.getCallback() != null) {
            builder.callback(request.getCallback());
        }

        builder.fetch(request.getUri());
    }

}
