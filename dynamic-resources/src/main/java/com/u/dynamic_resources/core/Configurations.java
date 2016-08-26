package com.u.dynamic_resources.core;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.u.dynamic_resources.internal.Cache;

import okhttp3.OkHttpClient;

/**
 * Created by saguilera on 8/24/16.
 */
public class Configurations {

    private @Nullable OkHttpClient client;
    private @Nullable Cache cache;

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

        public Builder() {}

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
