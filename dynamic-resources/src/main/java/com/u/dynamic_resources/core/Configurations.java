package com.u.dynamic_resources.core;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.u.dynamic_resources.internal.Cache;
import com.u.dynamic_resources.internal.DiskCache;

import okhttp3.OkHttpClient;

/**
 * Configurations you can supply to the library to have your own custom classes instead of
 * the defaults
 *
 * Created by saguilera on 8/24/16.
 */
public class Configurations {

    private @Nullable OkHttpClient client;
    private @Nullable Cache cache;

    /**
     * Package method to create a default configuration
     * @param context with access to the resources
     * @return Builder
     */
    static Builder getDefault(Context context) {
        return new Builder()
                .okHttpClient(new OkHttpClient.Builder().build())
                .cache(new DiskCache(context));
    }

    /**
     * Method developers should use to create new Configurations.
     *
     * @param context with access to the resources
     * @return Builder
     */
    public static Builder create(Context context) {
        return getDefault(context);
    }

    /**
     * Private constructor
     * @param client okHttpClient
     * @param cache cache
     */
    private Configurations(@Nullable OkHttpClient client,
                           @Nullable Cache cache) {
        this.client = client;
        this.cache = cache;
    }

    public @Nullable OkHttpClient getClient() {
        return client;
    }

    public @Nullable Cache getCache() {
        return cache;
    }

    public Builder newBuilder() {
        Builder builder = new Builder();

        if (getClient() != null) {
            builder.okHttpClient(getClient());
        }

        if (getCache() != null) {
            builder.cache(getCache());
        }

        return builder;
    }

    public static class Builder {

        private OkHttpClient client = null;
        private Cache cache = null;

        private Builder() {}

        public Builder okHttpClient(@NonNull OkHttpClient client) {
            this.client = client;
            return this;
        }

        public Builder cache(@NonNull Cache cache) {
            this.cache = cache;
            return this;
        }

        public @NonNull Configurations build() {
            return new Configurations(client,
                    cache);
        }

    }

}
