package com.saantiaguilera.dynamic_resources.internal;

import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.saantiaguilera.dynamic_resources.internal.loading.FileCallback;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Inner class that performs the networking fetches of a request + cache calls if already exists
 * to avoid useless calls
 *
 * Created by saguilera on 8/25/16.
 */
final class Streamer {

    private @NonNull OkHttpClient client;

    /**
     * Not weak reference because probably the user will just execute and dont retain the instance
     * in a reference. Since we are working with bitmaps, theres a really nice chance the callback
     * gets gc'ed.
     *
     * It wont leak because we are the ones using it only :)
     */
    private @Nullable FileCallback callback;
    private @NonNull Uri uri;

    private @NonNull Cache cache;

    //For writing files
    private static final ExecutorService executor = Executors.newFixedThreadPool(1);

    /**
     * Creator of a streamer immutable object
     * @return Builder
     */
    public static Builder create() {
        return new Builder();
    }

    /**
     * Private constructor
     * @param client for networking calls
     * @param callback for status
     * @param cache cache used for the images
     * @param uri to fetch
     */
    @SuppressWarnings("ConstantConditions")
    private Streamer(@Nullable OkHttpClient client,
                     @Nullable FileCallback callback,
                     @Nullable Cache cache,
                     @NonNull Uri uri) {
        Validator.checkNullAndThrow(this, client, cache, uri);
        this.client = client;
        this.cache = cache;
        this.callback = callback;
        this.uri = uri;
    }

    /**
     * Fetch the given Uri from network if cache doesnt has it, else retreive it from cache
     * and callback to user.
     *
     * Runs on UI Thread or worker thread
     */
    private void fetch() {
        if (cache.contains(uri) && callback != null) {
            new Handler(Looper.getMainLooper()).postAtFrontOfQueue(new SuccessRunnable(cache.get(uri)));
            return;
        }

        //Dont cache in network, because we will have it downloaded.
        CacheControl cacheControl = new CacheControl.Builder()
                .noCache()
                .noStore()
                .build();

        Request request = new Request.Builder()
                .url(uri.toString())
                .cacheControl(cacheControl)
                .get().build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (!call.isCanceled() && callback != null) {
                    callback.onFailure(e);
                }
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if (!call.isCanceled()) {
                    executor.submit(new Runnable() {
                        @Override
                        public void run() {
                            if (response != null) {
                                Streamer.this.onResponse(response);
                            }
                        }
                    });
                }
            }
        });
    }

    /**
     * Method for successfully downloading the image
     * @param response from network
     */
    private void onResponse(@NonNull Response response) {
        try {
            File file;

            if (cache.contains(uri)) {
                file = cache.get(uri);
            } else {
                file = cache.put(uri, response.body().byteStream());
            }

            new Handler(Looper.getMainLooper()).postAtFrontOfQueue(new SuccessRunnable(file));
        } catch (Exception e) {
            new Handler(Looper.getMainLooper()).postAtFrontOfQueue(new FailureRunnable(e));
        }
    }

    /**
     * Package access inner builder class for creating a Streamer
     */
    static class Builder {

        private OkHttpClient client = null;
        private FileCallback callback = null;

        private Cache cache = null;

        /**
         * Empty constructor
         */
        Builder() {}

        /**
         * Set a custom networking client
         * @param client for network calls
         * @return Builder
         */
        public Builder client(@NonNull OkHttpClient client) {
            this.client = client;
            return this;
        }

        /**
         * Set custom callbacks for the status of the request
         * @param callback for receiving status updates
         * @return Builder
         */
        public Builder callback(@NonNull FileCallback callback) {
            this.callback = callback;
            return this;
        }

        /**
         * Set custom cache for managing the images in disk
         * @param cache used for managing the images in disk
         * @return Builder
         */
        public Builder cache(@NonNull Cache cache) {
            this.cache = cache;
            return this;
        }

        /**
         * Create a streamer and fetch the given uri
         * @param uri to fetch from network or cache if already exists
         * @return Streamer instance for the given params
         */
        public Streamer fetch(@NonNull Uri uri) {
            Validator.checkNullAndThrow(this, uri);

            Streamer streamer = new Streamer(client,
                    callback == null ? null : callback,
                    cache,
                    uri);
            streamer.fetch();
            return streamer;
        }

    }

    /**
     * Inner class for a Failure networking event
     */
    class FailureRunnable implements Runnable {
        private @NonNull Exception exception;

        FailureRunnable(@NonNull Exception e) {
            exception = e;
        }

        @Override
        public void run() {
            if (callback != null) {
                callback.onFailure(exception);
            }
        }
    }

    /**
     * Inner class for a Success networking event
     */
    class SuccessRunnable implements Runnable {
        private @Nullable File file;

        SuccessRunnable(@Nullable File file) {
            this.file = file;
        }

        @Override
        public void run() {
            if (callback != null && file != null) {
                callback.onSuccess(file);
            }
        }
    }

}
