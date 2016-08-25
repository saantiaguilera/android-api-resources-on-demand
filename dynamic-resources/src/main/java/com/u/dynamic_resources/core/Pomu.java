package com.u.dynamic_resources.core;

import android.content.Context;
import android.net.Uri;

import com.u.dynamic_resources.internal.loading.LoadingCallback;
import com.u.dynamic_resources.screen.ScreenDensity;
import com.u.dynamic_resources.screen.UrlDensityFormatter;

/**
 * Created by saguilera on 8/24/16.
 */
public final class Pomu {

    private static ScreenDensity dpi = null;

    public static void init(Context context) {
        init(context, null);
    }

    public static void init(Context context, Configurations configurations) {
        dpi = ScreenDensity.get(context.getResources());
    }

    public static Builder create() {
        if (dpi == null) throw new NullPointerException("No screen dpi available. Maybe you forgot to initialize Pomu??");

        return new Builder();
    }

    public static class Builder {

        private Uri uri;

        public Builder() {}

        public Builder parse(String url) {
            uri = Uri.parse(url);
            return this;
        }

        public Builder parse(Uri uri) {
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
        public Builder parse(String url, UrlDensityFormatter formatter) {
            this.uri = Uri.parse(String.format(url, formatter.from(dpi)));
            return this;
        }

        public Builder get(LoadingCallback listener) {

            return this;
        }
    }

}
