package com.u.dynamic_resources.internal;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.u.dynamic_resources.internal.loading.FileCallback;

import java.lang.ref.WeakReference;

/**
 * Request class for doing custom requests to the pipeline
 *
 * Use this class at your own risk. By default Pomu already has methods for the usual requests, to
 * avoid the developer having to do this by himself. But if you want a particular request done,
 * you can of course create it and fetch it through the pipeline by yourself :)
 *
 * Created by saguilera on 8/25/16.
 */
public class Request {

    private @Nullable WeakReference<FileCallback> callback;
    private @NonNull Uri uri;

    /**
     * Private constructor
     * @param callback if existent
     * @param uri to fetch
     */
    private Request(@Nullable FileCallback callback,
                    @NonNull Uri uri) {
        if (callback != null) {
            this.callback = new WeakReference<>(callback);
        } else {
            this.callback = null;
        }

        this.uri = uri;
    }

    /**
     * Getter for the uri to fetch
     * @return uri
     */
    @NonNull
    public Uri getUri() {
        return uri;
    }

    /**
     * Getter for the callback if exists
     * @return callback
     */
    @Nullable
    public FileCallback getCallback() {
        return callback == null ? null : callback.get();
    }

    /**
     * Builder class for creating custom requests
     */
    public static class Builder {

        private WeakReference<FileCallback> callback;
        private Uri uri;

        /**
         * Empty constructor
         */
        public Builder() {}

        /**
         * Set a callback to the request
         * @param callback for receiving notifications about the status
         * @return Builder
         */
        public Builder callback(@NonNull FileCallback callback) {
            this.callback = new WeakReference<>(callback);
            return this;
        }

        /**
         * Set the uri to fetch
         * @param uri to fetch
         * @return Builder
         */
        public Builder uri(@NonNull Uri uri) {
            this.uri = uri;
            return this;
        }

        /**
         * Build a Request with the given params
         * @return Request
         */
        public Request build() {
            Validator.checkNullAndThrow(this, uri);

            return new Request(callback.get(), uri);
        }

    }

}
