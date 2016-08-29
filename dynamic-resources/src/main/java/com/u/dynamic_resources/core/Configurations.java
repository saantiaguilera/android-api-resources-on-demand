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

    /**
     * Getter for the networking client
     * @return Client if existing
     */
    public @Nullable OkHttpClient getClient() {
        return client;
    }

    /**
     * Getter for the cache
     * @return cache if existing
     */
    public @Nullable Cache getCache() {
        return cache;
    }

    /**
     * Create a new builder with this configurations
     * @return Builder
     */
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

    /**
     * Class for building configurations
     */
    public static class Builder {

        private OkHttpClient client = null;
        private Cache cache = null;

        private Builder() {}

        /**
         * Setter for a networking client
         * @param client for networking requests
         * @return Builder
         */
        public Builder okHttpClient(@NonNull OkHttpClient client) {
            this.client = client;
            return this;
        }

        /**
         * Setter for the cache used to store images in disk
         * @param cache cache
         * @return Builder
         */
        public Builder cache(@NonNull Cache cache) {
            this.cache = cache;
            return this;
        }

        /**
         * Build a new instance of Configurations. This should be supplied to Pomu or the Pipeline
         * if already initialized
         *
         * @return Builder
         */
        public @NonNull Configurations build() {
            return new Configurations(client,
                    cache);
        }

    }

}
