package com.u.dynamic_resources.internal;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.u.dynamic_resources.internal.loading.FileCallback;

import java.lang.ref.WeakReference;

/**
 * Created by saguilera on 8/25/16.
 */
public class Request {

    private @NonNull WeakReference<Context> context;
    private @Nullable WeakReference<FileCallback> callback;
    private @NonNull Uri uri;

    private Request(@NonNull Context context,
                    @Nullable FileCallback callback,
                    @NonNull Uri uri) {
        this.context = new WeakReference<>(context);

        if (callback != null) {
            this.callback = new WeakReference<>(callback);
        } else {
            this.callback = null;
        }

        this.uri = uri;
    }

    @NonNull
    public Uri getUri() {
        return uri;
    }

    @NonNull
    public Context getContext() {
        return context.get();
    }

    @Nullable
    public FileCallback getCallback() {
        return callback.get();
    }

    public static class Builder {

        private WeakReference<Context> context;
        private WeakReference<FileCallback> callback;
        private Uri uri;

        public Builder(@NonNull Context context) {
            this.context = new WeakReference<>(context);
        }

        public Builder callback(@NonNull FileCallback callback) {
            this.callback = new WeakReference<>(callback);
            return this;
        }

        public Builder uri(@NonNull Uri uri) {
            this.uri = uri;
            return this;
        }

        public Request build() {
            Validator.checkNull(this, context, uri);

            return new Request(context.get(), callback.get(), uri);
        }

    }

}
