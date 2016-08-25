package com.u.dynamic_resources.internal;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.annotation.WorkerThread;

import com.u.dynamic_resources.internal.loading.FileCallback;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by saguilera on 8/25/16.
 */
final class Streamer {

    private static final int BUFFER_SIZE = 1024;

    private @NonNull WeakReference<Context> context;

    private @NonNull OkHttpClient client;
    private @Nullable WeakReference<FileCallback> callback;
    private @NonNull Uri uri;
    private @NonNull File output;

    //For writing files
    private static final ExecutorService executor = Executors.newFixedThreadPool(1);

    public static Builder with(@NonNull Context context) {
        return new Builder(context);
    }

    private Streamer(@NonNull Context context,
                     @Nullable OkHttpClient client,
                     @Nullable FileCallback callback,
                     @NonNull Uri uri) {
        this.context = new WeakReference<>(context);

        if (client != null) {
            this.client = client;
        } else {
            this.client = new OkHttpClient.Builder().build();
        }

        if (callback != null) {
            this.callback = new WeakReference<>(callback);
        } else {
            this.callback = null;
        }

        this.uri = uri;
        this.output = Files.create(context, uri);
    }

    @UiThread
    private void fetch() {
        if (output.exists() && callback != null) {
            new Handler(Looper.getMainLooper()).postAtFrontOfQueue(new SuccessRunnable());
            return;
        }

        Request request = new Request.Builder()
                .url(uri.toString())
                .get().build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (!call.isCanceled() && callback != null) {
                    callback.get().onFailure(e);
                }
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if (!call.isCanceled()) {
                    executor.submit(new Runnable() {
                        @Override
                        public void run() {
                            write(response.body().byteStream());
                        }
                    });
                }
            }
        });
    }

    @WorkerThread
    private void write(InputStream is) {
        FileOutputStream fos = null;
        Handler handler = new Handler(Looper.getMainLooper());

        try {
            if (output.exists()) {
                //File already exists (was downloaded probably in parallel twice)
                handler.postAtFrontOfQueue(new SuccessRunnable());
                return;
            }

            fos = new FileOutputStream(output);
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead = 0;
            while ((bytesRead = is.read(buffer, 0, buffer.length)) >= 0) {
                fos.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            handler.postAtFrontOfQueue(new FailureRunnable(e));
        } finally {
            try {
                if (is != null) {
                    is.close();
                }

                if (fos != null) {
                    fos.close();
                }

                handler.postAtFrontOfQueue(new SuccessRunnable());
            } catch (Exception e) {
                handler.postAtFrontOfQueue(new FailureRunnable(e));
            }
        }
    }

    static class Builder {

        private WeakReference<Context> context = null;

        private OkHttpClient client = null;
        private WeakReference<FileCallback> callback = null;
        private Uri uri = null;

        Builder(@NonNull Context context) {
            this.context = new WeakReference<>(context);
        }

        public Builder client(@NonNull OkHttpClient client) {
            this.client = client;
            return this;
        }

        public Builder callback(@NonNull FileCallback callback) {
            this.callback = new WeakReference<>(callback);
            return this;
        }

        public Streamer fetch(@NonNull Uri uri) {
            Validator.checkNull(this, context, uri);

            Streamer streamer = new Streamer(context.get(),
                    client,
                    callback == null ? null : callback.get(),
                    uri);
            streamer.fetch();
            return streamer;
        }

    }

    class FailureRunnable implements Runnable {
        private Exception exception;

        FailureRunnable(Exception e) {
            exception = e;
        }

        @Override
        public void run() {
            if (callback != null) {
                callback.get().onFailure(exception);
            }
        }
    }

    class SuccessRunnable implements Runnable {
        @Override
        public void run() {
            if (callback != null) {
                callback.get().onSuccess(output);
            }
        }
    }

}
