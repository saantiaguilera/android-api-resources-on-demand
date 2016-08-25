package com.u.dynamic_resources.core;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import okhttp3.OkHttpClient;

/**
 * Created by saguilera on 8/24/16.
 */
public class Configurations {

    private @Nullable OkHttpClient client;

    private Configurations(@Nullable OkHttpClient client) {
        this.client = client;
    }

    public @Nullable OkHttpClient getClient() {
        return client;
    }

    public Builder newBuilder() {
        return new Builder()
                .okHttpClient(client);
    }

    public static class Builder {

        private OkHttpClient client;

        public Builder() {}

        public Builder okHttpClient(@NonNull OkHttpClient client) {
            this.client = client;
            return this;
        }

        public @NonNull Configurations build() {
            return new Configurations(client);
        }

    }

}
