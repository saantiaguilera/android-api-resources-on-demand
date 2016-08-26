package com.u.dynamic_resources.internal;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.u.dynamic_resources.internal.loading.FileCallback;

import java.lang.ref.WeakReference;

/**
 * Created by saguilera on 8/25/16.
 */
public class Request {

    private @Nullable WeakReference<FileCallback> callback;
    private @NonNull Uri uri;

    private Request(@Nullable FileCallback callback,
                    @NonNull Uri uri) {
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

    @Nullable
    public FileCallback getCallback() {
        return callback == null ? null : callback.get();
    }

    public static class Builder {

        private WeakReference<FileCallback> callback;
        private Uri uri;

        public Builder() {}

        public Builder callback(@NonNull FileCallback callback) {
            this.callback = new WeakReference<>(callback);
            return this;
        }

        public Builder uri(@NonNull Uri uri) {
            this.uri = uri;
            return this;
        }

        public Request build() {
            Validator.checkNullAndThrow(this, uri);

            return new Request(callback.get(), uri);
        }

    }

}
