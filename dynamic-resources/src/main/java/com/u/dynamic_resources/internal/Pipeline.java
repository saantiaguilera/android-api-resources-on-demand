package com.u.dynamic_resources.internal;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.u.dynamic_resources.core.Configurations;

import okhttp3.OkHttpClient;

/**
 * Created by saguilera on 8/25/16.
 */
public class Pipeline {

    private static Pipeline instance;

    private @Nullable OkHttpClient client;
    private @Nullable Cache cache;

    public static @NonNull Pipeline getInstance() {
        if (instance == null) {
            synchronized (Pipeline.class) {
                if (instance == null) {
                    instance = new Pipeline();
                }
            }
        }

        return instance;
    }

    private Pipeline() {
        client = null;
    }

    public void setConfigurations(@NonNull Configurations configurations) {
        this.client = configurations.getClient();
        this.cache = configurations.getCache();
    }

    @SuppressWarnings("ConstantConditions")
    public void fetch(@NonNull Request request) {
        Streamer.Builder builder = Streamer.with(request.getContext())
                .cache(cache);

        if (client != null) {
            builder.client(client);
        }

        if (request.getCallback() != null) {
            builder.callback(request.getCallback());
        }

        builder.fetch(request.getUri());
    }

}
