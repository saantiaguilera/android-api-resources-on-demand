package com.u.dynamic_resources.core;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.ImageView;

import com.facebook.drawee.view.DraweeView;
import com.facebook.drawee.view.SimpleDraweeView;
import com.u.dynamic_resources.internal.Pipeline;
import com.u.dynamic_resources.internal.Request;
import com.u.dynamic_resources.internal.Validator;
import com.u.dynamic_resources.internal.fresco.FrescoImageController;
import com.u.dynamic_resources.internal.loading.BitmapCallback;
import com.u.dynamic_resources.internal.loading.FileCallback;
import com.u.dynamic_resources.screen.ScreenDensity;
import com.u.dynamic_resources.screen.UrlDensityFormatter;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Core class of the library. From here requests are created and dispatched by the developer.
 *
 * This is the access point of the developer to the lib.
 *
 * Created by saguilera on 8/24/16.
 */
public final class Pomu {

    private static ScreenDensity dpi = null;

    /**
     * Initialize Pomu with the default configurations.
     * Be sure to always call the initialize() method on the Application's onCreate()
     * so you are certain that every Pomu call across the application will be cohesive.
     *
     * @param context of the application
     */
    public static void initialize(@NonNull Context context) {
        initialize(context, Configurations.getDefault(context).build());
    }

    /**
     * Initialize Pomu with custom configurations.
     * Be sure to always call the initialize() method on the Application's onCreate()
     * so you are certain that every Pomu call across the application will be cohesive.
     *
     * You can change your configurations at any time by yourself by setting them to the Pipeline
     * with Pipeline.getInstance().setConfigurations(configs). This is not recommended tho since
     * it will leave a part of your app with old configurations and another one with newer.
     *
     * @param context of the application
     * @param configs custom to set to Pomu
     */
    public static void initialize(@NonNull Context context, @NonNull Configurations configs) {
        dpi = ScreenDensity.get(context.getResources());
        Pipeline.getInstance().setConfigurations(configs);
    }

    /**
     * Create a new Pomu.Builder. This is used every time you want to create a new request
     * to either just download an image or even show it in a View.
     *
     * Be sure to have initialized Pomu before calling this.
     * Supply any kind of ContextWrapper that has permissions to the resources
     *
     * @param context
     * @return
     */
    public static @NonNull Builder create(@NonNull Context context) {
        if (dpi == null) throw new NullPointerException("No screen dpi available. Maybe you forgot to initialize Pomu??");

        return new Builder(context);
    }

    /**
     * Builder class to create new Pomu requests
     */
    public static class Builder {

        private @NonNull WeakReference<Context> context;

        private @Nullable List<Uri> uris;
        private @Nullable WeakReference<BitmapCallback> callback;
        private @Nullable FrescoImageController.Builder controller;

        /**
         * Package visible constructor. Create Builders with Pomu.create(context).
         *
         * @param context with access to the resources
         */
        Builder(@NonNull Context context) {
            this.context = new WeakReference<>(context);
            this.uris = null;
        }

        /**
         * Get all the uris to download
         * @return uris to download
         */
        private @NonNull List<Uri> getUris() {
            if (uris == null) {
                return (uris = new ArrayList<>());
            }

            return uris;
        }

        /**
         * Add a url to download. This creates a list of urls to downlaod, meaning you can call in a
         * single request 4 times this and it will download 4 images.
         *
         * @param url to add to the download queue
         * @return Builder
         */
        public Builder url(@NonNull String url) {
            getUris().add(Uri.parse(url));
            return this;
        }

        /**
         * Add a url to download. This creates a list of urls to downlaod, meaning you can call in a
         * single request 4 times this and it will download 4 images.
         *
         * @param uri to add to the download queue
         * @return Builder
         */
        public Builder url(@NonNull Uri uri) {
            getUris().add(uri);
            return this;
        }

        /**
         * If your backend or you already have a set of images that verify only change a specific arg in the url
         * It facilitates a lot the loading.
         *
         * Eg. All your images are with the format
         * http://myapi.com/static-image-:image_id-:dpi.jpg
         *  Where:
         *       - :image_id is a particular image (idk, you have 100 images, goes from 1 to 100)
         *       - :dpi is the dpi in which you want the image, also could be a :resolution eg 1200x800
         *    This can vary as much as your imagination wants.
         *
         * With this you should implement the contract once, and inside:
         *    public String from(ScreenDensity screenDensity) {
         *        switch (screenDensity.getDensity()) {
         *            case ...:
         *                 return "1200x800";
         *            ...
         *        }
         *    }
         *
         * This will be used then in all the images, and format them for you (not having to do this your own
         * or generate boilerplate with switches and densities all the time).
         *
         * Note: Be sure that the string accepts formatting (this means, have a %s where the
         * :dpi will go).
         *
         * @param url
         * @param formatter
         * @return
         */
        public Builder url(@NonNull String url, @NonNull UrlDensityFormatter formatter) {
            getUris().add(Uri.parse(String.format(url, formatter.from(dpi))));
            return this;
        }

        /**
         * Callback to listen to the status of the request. If multiple uris are used, this callback
         * will be called for each uri status.
         *
         * @param callback to get notified of the request event
         * @return Builder
         */
        public Builder callback(@NonNull BitmapCallback callback) {
            this.callback = new WeakReference<>(callback);
            return this;
        }

        /**
         * If using Fresco, you can supply a controller to customize the Image request prior to the decode
         *
         * @param controller to customize fresco download
         * @return Builder
         */
        public Builder controller(@NonNull FrescoImageController.Builder controller) {
            this.controller = controller;
            return this;
        }

        /**
         * Create a file callback to add the File received to an ImageView
         *
         * @param view where the image will be shown
         * @return FileCallback
         */
        private FileCallback createRequestCallback(@NonNull final ImageView view) {
            return new FileCallback() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    if (callback != null) {
                        callback.get().onFailure(e);
                    }
                }

                @Override
                public void onSuccess(@NonNull File file) {
                    if (view instanceof DraweeView) {
                        //If fresco is available, take advantage of it
                        FrescoImageController.Builder builder = controller;

                        if (builder == null) {
                            builder = FrescoImageController.create(context.get());
                        }

                        builder.load(file.toURI().toString()) //Its the same to do this or use the schema: "file://" + file.getPath()
                                .noDiskCache() // Because doh
                                .listener(new FrescoImageController.Callback() {
                                    //This is why I hate not using eventbus and getting callback hells :)
                                    @Override
                                    public void onSuccess() {
                                        if (callback != null) {
                                            callback.get().onSuccess();
                                        }
                                    }

                                    @Override
                                    public void onFailure(@NonNull Throwable t) {
                                        if (callback != null) {
                                            callback.get().onFailure(t);
                                        }
                                    }
                                }).into((DraweeView) view);
                    } else {
                        //If its not, decode normally and set it (in Dalvik systems be careful with this)
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

                        view.setImageBitmap(BitmapFactory.decodeFile(file.getPath(), options));

                        if (callback != null) {
                            callback.get().onSuccess();
                        }
                    }
                }
            };
        }

        /**
         * Download the selected uris and store them in the cache. This method wont show any of them.
         *
         * Its useful if you need some images to be downloaded at the start of the application or at previous
         * steps where you still dont have the view, but as soon as you will have it you want the user
         * to see the image instantly (without waiting for it to download)
         */
        @SuppressWarnings("ConstantConditions")
        public void get() {
            Validator.checkNullAndThrow(this, uris);

            if (!uris.isEmpty()) {
                for (Uri uri : uris) {
                    Validator.checkNullAndThrow(this, uri);

                    Request request = new Request.Builder()
                            .uri(uri)
                            .build();

                    Pipeline.getInstance().fetch(request);
                }
            } else {
                throw new IllegalStateException("No uris provided for Pomu get(). Forgot to call url()??");
            }
        }

        /**
         * Download a single uri and show the image in an ImageView.
         *
         * Please beware this should be call with only a single uri. Having multiple uris will throw a
         * {@link IllegalStateException} because we cant show more than one image in a single view (duh)
         *
         * This method will download the image, cache it so we dont have to do it again and instantly
         * show it in a imageView.
         *
         * If you are using Fresco, this method will take advantage of it.
         *
         * @param view where the image will be shown
         */
        @SuppressWarnings("ConstantConditions")
        public void into(@NonNull final ImageView view) {
            Validator.checkNullAndThrow(this, uris);

            if (uris.size() == 1) {
                Uri uri = uris.get(0);
                Validator.checkNullAndThrow(this, uri);

                Request request = new Request.Builder()
                        .uri(uri)
                        .callback(createRequestCallback(view))
                        .build();

                Pipeline.getInstance().fetch(request);
            } else {
                throw new IllegalStateException("Size of uris to put in ImageView is " + uris.size() + ". Please be sure to provide only 1 uri, we cant show multiple images in a single view (Neither zero)");
            }
        }
    }

}
