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

/**
 * Created by saguilera on 8/24/16.
 */
public final class Pomu {

    private static ScreenDensity dpi = null;
    private static Configurations configurations = null;

    public static void initialize(@NonNull Context context) {
        initialize(context, null);
    }

    public static void initialize(@NonNull Context context, @Nullable Configurations configs) {
        dpi = ScreenDensity.get(context.getResources());
        configurations = configs;
    }

    public static @Nullable Configurations getConfigurations() {
        return configurations;
    }

    public static @NonNull Builder create(@NonNull Context context) {
        if (dpi == null) throw new NullPointerException("No screen dpi available. Maybe you forgot to initialize Pomu??");

        return new Builder(context);
    }

    public static class Builder {

        private @NonNull WeakReference<Context> context;

        private @NonNull Uri uri;
        private @Nullable WeakReference<BitmapCallback> callback;
        private @Nullable FrescoImageController.Builder controller;

        Builder(@NonNull Context context) {
            this.context = new WeakReference<>(context);
        }

        public Builder parse(@NonNull String url) {
            uri = Uri.parse(url);
            return this;
        }

        public Builder parse(@NonNull Uri uri) {
            this.uri = uri;
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
        public Builder parse(@NonNull String url, @NonNull UrlDensityFormatter formatter) {
            this.uri = Uri.parse(String.format(url, formatter.from(dpi)));
            return this;
        }

        public Builder callback(@NonNull BitmapCallback callback) {
            this.callback = new WeakReference<>(callback);
            return this;
        }

        public Builder controller(@NonNull FrescoImageController.Builder controller) {
            this.controller = controller;
            return this;
        }

        public void into(@NonNull final ImageView view) {
            Validator.checkNull(this, uri);

            Request request = new Request.Builder(context.get())
                    .uri(uri)
                    .callback(new FileCallback() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            if (callback != null) {
                                callback.get().onFailure(e);
                            }
                        }

                        @Override
                        public void onSuccess(@NonNull File file) {
                            if (view instanceof SimpleDraweeView) {
                                //If fresco is available, take advantage of it
                                FrescoImageController.Builder builder = controller;

                                if (builder == null) {
                                    builder = FrescoImageController.create(context.get());
                                }

                                builder.load(file.toURI().toString()) //Its the same to do this or use the schema: "file://" + file.getPath()
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
                    }).build();

            Pipeline.getInstance().fetch(request);
        }
    }

}
