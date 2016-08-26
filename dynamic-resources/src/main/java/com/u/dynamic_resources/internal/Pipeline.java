package com.u.dynamic_resources.internal;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.u.dynamic_resources.core.Configurations;

/**
 * Created by saguilera on 8/25/16.
 */
public class Pipeline {

    private static Pipeline instance;

    private @Nullable Configurations configurations = null;

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

    private Pipeline() {}

    public void setConfigurations(@Nullable Configurations configurations) {
        this.configurations = configurations;
    }

    @SuppressWarnings("ConstantConditions")
    public void fetch(@NonNull Request request) {
        Streamer.Builder builder = Streamer.with(request.getContext());

        if (!Validator.checkNull(configurations, configurations.getClient())) {
            builder.client(configurations.getClient());
        }

        if (request.getCallback() != null) {
            builder.callback(request.getCallback());
        }

        builder.fetch(request.getUri());
    }

}
