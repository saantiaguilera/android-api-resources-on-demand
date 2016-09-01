package com.saantiaguilera.dynamic_resources.internal;

import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.SparseArray;
import android.widget.ImageView;

import com.saantiaguilera.dynamic_resources.internal.loading.FileCallback;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
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

    private @Nullable static List<Call> currentCalls = null;

    //For writing files
    private static final ExecutorService executor = Executors.newFixedThreadPool(1);

    /**
     * Creator of a streamer immutable object
     * @return Builder
     */
    public static Builder create() {
        return new Builder();
    }

    private static List<Call> getCurrentCalls() {
        if (currentCalls == null) {
            synchronized (Streamer.class) {
                if (currentCalls == null) {
                    currentCalls = new CopyOnWriteArrayList<>();
                }
            }
        }

        return currentCalls;
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
    @SuppressWarnings("SuspiciousMethodCalls")
    private void fetch() {
        if (cache.contains(uri) && callback != null) {
            new Handler(Looper.getMainLooper()).postAtFrontOfQueue(
                    new SuccessRunnable(new Call(null, callback), cache.get(uri)));
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

        okhttp3.Call call = client.newCall(request);
        Call currentCall = get(call);

        if (currentCall != null) {
            //If the request is already executing for this uri, avoid doing it more than once at the same time
            if (callback != null) {
                currentCall.add(callback);
            }
        } else {
            getCurrentCalls().add(new Call(call, callback));
            call.enqueue(new okhttp3.Callback() {
                @Override
                public void onFailure(okhttp3.Call call, IOException e) {
                    if (!call.isCanceled()) {
                        new Handler(Looper.getMainLooper()).post(new FailureRunnable(get(call), e));
                    }
                }

                @Override
                public void onResponse(final okhttp3.Call call, final Response response) throws IOException {
                    if (!call.isCanceled()) {
                        executor.submit(new Runnable() {
                            @Override
                            public void run() {
                                if (response != null) {
                                    Streamer.this.onResponse(call, response);
                                }
                            }
                        });
                    }
                }
            });
        }
    }

    /**
     * Method for successfully downloading the image
     * @param response from network
     */
    private void onResponse(@NonNull okhttp3.Call call, @NonNull Response response) {
        try {
            File file;

            if (cache.contains(uri)) {
                file = cache.get(uri);
            } else {
                file = cache.put(uri, response.body().byteStream());
            }

            new Handler(Looper.getMainLooper()).post(new SuccessRunnable(get(call), file));
        } catch (Exception e) {
            new Handler(Looper.getMainLooper()).post(new FailureRunnable(get(call), e));
        }
    }

    private @Nullable Call get(okhttp3.Call call) {
        for (Call obj : getCurrentCalls()) {
            if (obj.equals(call)) {
                return obj;
            }
        }

        return null;
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
    static class FailureRunnable implements Runnable {
        private @NonNull Exception exception;
        private @Nullable Call call;

        FailureRunnable(@Nullable Call call, @NonNull Exception e) {
            this.exception = e;
            this.call = call;
        }

        @Override
        public void run() {
            if (call != null) {
                for (FileCallback callback : call.getCallbacks()) {
                    if (callback != null) {
                        callback.onFailure(exception);
                    }
                }

                // To avoid possible mem leaks
                call.getCallbacks().clear();
                // Remove it to avoid staggering...
                getCurrentCalls().remove(call);
            }
        }
    }

    /**
     * Inner class for a Success networking event
     */
    static class SuccessRunnable implements Runnable {
        private @Nullable File file;
        private @Nullable Call call;

        SuccessRunnable(@Nullable Call call, @Nullable File file) {
            this.file = file;
            this.call = call;
        }

        @Override
        public void run() {
            if (call != null && file != null) {
                for (FileCallback callback : call.getCallbacks()) {
                    if (callback != null) {
                        callback.onSuccess(file);
                    }
                }

                // To avoid possible mem leaks
                call.getCallbacks().clear();
                // Remove it to avoid staggering...
                getCurrentCalls().remove(call);
            }
        }
    }

    static class Call {

        private @Nullable okhttp3.Call call;
        private @NonNull List<FileCallback> callbacks;

        public Call(@Nullable okhttp3.Call call, @Nullable FileCallback callback) {
            this.call = call;
            this.callbacks = new CopyOnWriteArrayList<>();

            if (callback != null) {
                this.callbacks.add(callback);
            }
        }

        public void add(@NonNull FileCallback callback) {
            callbacks.add(callback);
        }

        @SuppressWarnings("all") //Complaints about simlify and a null check that its already checked..
        @Override
        public boolean equals(Object obj) {
            if (call == null) {
                return false;
            }

            if (obj instanceof Call) {
                if (((Call) obj).call != null) {
                    return call.request().url().equals(((Call) obj).call.request().url());
                }
            }

            if (obj instanceof okhttp3.Call) {
                return call.request().url().equals(((okhttp3.Call) obj).request().url());
            }

            return false;
        }

        public @NonNull List<FileCallback> getCallbacks() {
            return callbacks;
        }

    }

}
