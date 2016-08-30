package com.saantiaguilera.dynamic_resources.internal;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.saantiaguilera.dynamic_resources.core.Configurations;

import okhttp3.OkHttpClient;

/**
 * Pipeline to send requests to a streamer and manage cache/stuff
 * Created by saguilera on 8/25/16.
 */
public class Pipeline {

    private static Pipeline instance;

    private @Nullable OkHttpClient client;
    private @Nullable Cache cache;

    /**
     * Singleton instance for the pipeline
     * @return Pipeline instance
     */
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

    /**
     * Private constructor
     */
    private Pipeline() {
        client = null;
        cache = null;
    }

    /**
     * Set custom configurations to the pipeline
     *
     * @param configurations to set to the pipeline
     */
    public void setConfigurations(@NonNull Configurations configurations) {
        this.client = configurations.getClient();
        this.cache = configurations.getCache();
    }

    /**
     * Clear all the current data cached on disk
     */
    public void clearCaches() {
        if (cache != null) {
            cache.clear();
        }
    }

    /**
     * Fetch a particular request
     * @param request with the data to fetch
     */
    @SuppressWarnings("ConstantConditions")
    public void fetch(@NonNull Request request) {
        Streamer.Builder builder = Streamer.create();

        if (cache != null) {
            builder.cache(cache);
        }

        if (client != null) {
            builder.client(client);
        }

        if (request.getCallback() != null) {
            builder.callback(request.getCallback());
        }

        builder.fetch(request.getUri());
    }

}
