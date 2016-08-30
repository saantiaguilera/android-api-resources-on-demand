package com.saantiaguilera.dynamic_resources.internal;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.AsyncLayoutInflater;

import com.facebook.imagepipeline.core.PriorityThreadFactory;
import com.saantiaguilera.dynamic_resources.core.Configurations;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;

/**
 * Pipeline to send requests to a streamer and manage cache/stuff
 * Created by saguilera on 8/25/16.
 */
public class Pipeline {

    private static Pipeline instance;

    private @Nullable OkHttpClient client;
    private @Nullable Cache cache;

    private @Nullable Executor executor;

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
        executor = null;
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

    private @NonNull Executor getSingleThreadPoolExecutor() {
        if (executor == null) {
            synchronized (this) {
                if (executor == null) {
                    executor = Executors.newFixedThreadPool(1,
                            new PriorityThreadFactory(Process.THREAD_PRIORITY_BACKGROUND));
                }
            }
        }

        return executor;
    }

    /**
     * Clear all the current data cached on disk
     */
    public void removeAllCaches() {
        if (cache != null) {
            getSingleThreadPoolExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    cache.clear();
                }
            });
        }
    }

    /**
     * Remove a particular key from the cache
     * @param key uri to remove
     */
    public void removeCache(final Uri key) {
        if (cache != null) {
            getSingleThreadPoolExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    cache.remove(key);
                }
            });
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
