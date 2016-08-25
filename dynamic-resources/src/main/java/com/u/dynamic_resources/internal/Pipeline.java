package com.u.dynamic_resources.internal;

import com.u.dynamic_resources.core.Configurations;

import okhttp3.OkHttpClient;

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

    public void fetch(Request request) {
        Streamer.Builder builder = Streamer.with(request.getContext());
                //.client(configurations.getClient())

        if (request.getCallback() != null) {
            builder.callback(request.getCallback());
        }

        builder.fetch(request.getUri());
    }

}
