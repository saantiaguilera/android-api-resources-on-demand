package com.u.dynamic_resources.internal.fresco;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.os.OperationCanceledException;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.DraweeView;
import com.facebook.imagepipeline.common.ImageDecodeOptions;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.facebook.imagepipeline.request.Postprocessor;

import java.io.File;
import java.lang.ref.WeakReference;

/**
 * Immutable utility class to load an image with a desired callback.
 *
 * Simple usage example;

 FrescoImageController.create(someContext)
 .load(uriToLoadImageFrom); //Supports uri/url/resId/file
 .listener(new FrescoImageController.Callback() {
 //Override the onSuccess and the onFailure and do what you want
 })
 //There are a lot more stuff, check it out
 .attach(theView);

 *
 * Created by saguilera on 8/1/16.
 */
public class FrescoImageController {

    private WeakReference<Context> contextRef;

    private @NonNull WeakReference<? extends DraweeView> view;
    private @Nullable WeakReference<Callback> callback;
    private @NonNull Uri uri;

    private @Nullable ResizeOptions resizeOptions;
    private @Nullable ImageDecodeOptions decodeOptions;
    private @Nullable Postprocessor postprocessor;

    private boolean rotate;
    private boolean tapToRetry;
    private boolean progressiveRendering;
    private boolean localThumbnailPreview;

    private boolean noCache;
    private boolean noDiskCache;
    private boolean noMemmoryCache;

    /**
     * Static method to create an empty builder. The same can be achieved by doing
     * new FrescoImageController.Builder();
     *
     * @return empty builder
     */
    public static Builder create(Context context) {
        return new Builder(context);
    }

    /**
     * Private constructor. Since its an immutable object, use builders.
     */
    private FrescoImageController(@NonNull WeakReference<Context> contextRef,
                                  @NonNull final Uri uri, @NonNull DraweeView view,
                                  @Nullable WeakReference<Callback> wrCallback,
                                  @Nullable ResizeOptions resizeOpt, @Nullable ImageDecodeOptions decodeOpt,
                                  @Nullable Postprocessor postprocessor,
                                  boolean rotate, boolean ttr, boolean pr, boolean ltp,
                                  boolean noCache, boolean noDiskCache, boolean noMemmoryCache) {
        this.contextRef = contextRef;

        this.view = new WeakReference<>(view);
        this.callback = wrCallback;
        this.uri = uri;

        this.resizeOptions = resizeOpt;
        this.decodeOptions = decodeOpt;
        this.postprocessor = postprocessor;

        this.rotate = rotate;
        this.tapToRetry = ttr;
        this.progressiveRendering = pr;
        this.localThumbnailPreview = ltp;

        this.noCache = noCache;
        this.noDiskCache = noDiskCache;
        this.noMemmoryCache = noMemmoryCache;

        ControllerListener<ImageInfo> listener = new BaseControllerListener<ImageInfo>() {
            @Override
            public void onFinalImageSet(String id, ImageInfo imageInfo, Animatable animatable) {
                super.onFinalImageSet(id, imageInfo, animatable);

                if (callback != null && callback.get() != null) {
                    callback.get().onSuccess();
                }

                if (!isCacheEnabled() || !isMemmoryCacheEnabled()) {
                    Fresco.getImagePipeline().evictFromCache(getUri());
                }
            }

            @Override
            public void onFailure(String id, Throwable throwable) {
                super.onFailure(id, throwable);

                if (callback != null && callback.get() != null) {
                    callback.get().onFailure(throwable);
                }
            }
        };

        ImageRequestBuilder request = ImageRequestBuilder.newBuilderWithSource(uri)
                .setAutoRotateEnabled(rotate)
                .setLocalThumbnailPreviewsEnabled(localThumbnailPreview)
                .setProgressiveRenderingEnabled(progressiveRendering);

        if (noCache || noDiskCache) {
            request.disableDiskCache();
        }

        if (postprocessor != null) {
            request.setPostprocessor(postprocessor);
        }

        if (decodeOptions != null) {
            request.setImageDecodeOptions(decodeOptions);
        }

        if (resizeOptions != null) {
            request.setResizeOptions(resizeOptions);
        }

        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setUri(uri)
                .setImageRequest(request.build())
                .setTapToRetryEnabled(tapToRetry)
                .setOldController(view.getController())
                .setControllerListener(listener)
                .build();

        view.setController(controller);
    }

    /**
     * gets the attached view
     *
     * @return attached view or null if its already gced by the os
     */
    public @Nullable DraweeView getView() {
        return view.get();
    }

    /**
     * Getter
     */
    public @Nullable ResizeOptions getResizeOptions() {
        return resizeOptions;
    }

    /**
     * Getter
     */
    public @Nullable Postprocessor getPostprocessor() {
        return postprocessor;
    }

    /**
     * Getter
     */
    public @Nullable ImageDecodeOptions getDecodeOptions() {
        return decodeOptions;
    }

    /**
     * Getter
     */
    @NonNull
    public Uri getUri() {
        return uri;
    }

    /**
     * Getter
     */
    public boolean isCacheEnabled() {
        return !noCache;
    }

    /**
     * Getter
     */
    public boolean isMemmoryCacheEnabled() {
        return !noMemmoryCache;
    }

    /**
     * Getter
     */
    public boolean isDiskCacheEnabled() {
        return !noDiskCache;
    }

    /**
     * Getter
     */
    public boolean isLocalThumbnailPreviewEnabled() {
        return localThumbnailPreview;
    }

    /**
     * Getter
     */
    public boolean isProgressiveRenderingEnabled() {
        return progressiveRendering;
    }

    /**
     * Getter
     */
    public boolean isAutoRotateEnabled() {
        return rotate;
    }

    /**
     * Getter
     */
    public boolean isTapToRetryEnabled() {
        return tapToRetry;
    }

    /**
     * Perform an explicit success callback
     */
    public void success() {
        if (callback != null && callback.get() != null) {
            callback.get().onSuccess();
        }
    }

    /**
     * Perform an explicit failure callback
     */
    public void failure() {
        if (callback != null && callback.get() != null) {
            callback.get().onFailure(new OperationCanceledException("Called failure explicitly from " + getClass().getSimpleName()));
        }
    }

    /**
     * Create builder from state.
     *
     * Note this wont set the current view.
     *
     * @return new builder with current state
     */
    public @NonNull Builder newBuilder() {
        Builder builder = new Builder(contextRef.get())
                .load(getUri())
                .autoRotate(isAutoRotateEnabled())
                .tapToRetry(isTapToRetryEnabled())
                .progressiveRendering(isProgressiveRenderingEnabled())
                .localThumbnailPreview(isLocalThumbnailPreviewEnabled());

        if (!isCacheEnabled()) {
            builder.noCache();
        }

        if (!isDiskCacheEnabled()) {
            builder.noDiskCache();
        }

        if (!isMemmoryCacheEnabled()) {
            builder.noMemmoryCache();
        }

        if (getDecodeOptions() != null) {
            builder.decodeOptions(getDecodeOptions());
        }

        if (getResizeOptions() != null) {
            builder.resize(getResizeOptions().width, getResizeOptions().height);
        }

        if (getPostprocessor() != null) {
            builder.postprocessor(getPostprocessor());
        }

        if (callback != null && callback.get() != null) {
            builder.listener(callback.get());
        }

        return builder;
    }

    /**
     * Builder class to create an immutable FrescoController
     */
    public static class Builder {

        private @NonNull WeakReference<Context> contextRef;

        private @Nullable Uri mUri = null;
        private @Nullable WeakReference<Callback> listener = null;
        private @Nullable ResizeOptions resizeOptions = null;
        private boolean rotate = false;
        private boolean tapToRetry = false;
        private boolean progressiveRendering = false;
        private boolean localThumbnailPreview = false;
        private boolean noCache = false;
        private boolean noDiskCache = false;
        private boolean noMemmoryCache = false;
        private @Nullable ImageDecodeOptions decodeOptions = null;
        private @Nullable Postprocessor postprocessor = null;

        /**
         * Constructor
         */
        public Builder(@NonNull Context context) {
            this.contextRef = new WeakReference<>(context);
        }

        /**
         * Set a resId from where the image will be loaded
         * @param resId with the id of the drawable to load
         * @return Builder
         */
        public Builder load(int resId) {
            this.mUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
                    contextRef.get().getResources().getResourcePackageName(resId) + '/' +
                    contextRef.get().getResources().getResourceTypeName(resId) + '/' +
                    contextRef.get().getResources().getResourceEntryName(resId));
            return this;
        }

        /**
         * Set Uri from where the image will be loaded
         * @param uri with the address to download the image from
         * @return Builder
         */
        public Builder load(@NonNull Uri uri) {
            this.mUri = uri;
            return this;
        }

        /**
         * Set Url from where the image will be loaded
         * @param url with the address to download the image from
         * @return Builder
         */
        public Builder load(@NonNull String url) {
            this.mUri = Uri.parse(url);
            return this;
        }

        /**
         * Set file from where the image will be loaded
         * @param file with the image
         * @return Builder
         */
        public Builder load(@NonNull File file) {
            this.mUri = Uri.fromFile(file);
            return this;
        }

        /**
         * Set if you want to receive callbacks from the loading.
         *
         * @param listener from where callbacks will be observed
         * @return Builder
         */
        public Builder listener(@NonNull Callback listener) {
            this.listener = new WeakReference<>(listener);
            return this;
        }

        /**
         * Resize the image before showing it.
         * Of course resize != scale.
         * For scaling just use the layout_width / layout_height of the view.
         *
         * Note: Currently fresco doesnt support png resizing. So take into account that if you will
         * implement this in a place with possible .png images (like local user files) you wont have
         * all images resized. This can lead to problems, eg if you have a jpg and a png image of
         * 16:9 and you resize to 1:1, you will only resize the squared one.
         * A possible solution for this, but please use it carefully is to use a postprocessor that
         * resizes the bitmap if its Uri is a .png, but be careful it can make the cache useless
         *
         * @param width dest to resize
         * @param height dest to resize
         * @return Builder
         */
        public Builder resize(int width, int height) {
            this.resizeOptions = new ResizeOptions(width, height);
            return this;
        }

        /**
         * Helper that autorotates the image depending on the exif value
         * Default: False
         *
         * @param should auto rotate
         * @return Builder
         */
        public Builder autoRotate(boolean should) {
            this.rotate = should;
            return this;
        }

        /**
         * Dont cache the image neither in disk nor image.
         * By default all images are cached
         *
         * @return Builder
         */
        public Builder noCache() {
            this.noCache = true;
            return this;
        }

        /**
         * Dont cache the image in disk.
         * By default all images are cached
         *
         * @return Builder
         */
        public Builder noDiskCache() {
            this.noDiskCache = true;
            return this;
        }

        /**
         * Dont cache the image in memmory
         * By default all images are cached
         *
         * @return Builder
         */
        public Builder noMemmoryCache() {
            this.noMemmoryCache = true;
            return this;
        }

        /**
         * Tap on the image to retry loading it
         * Default: False
         *
         * @param should enable tap to retry
         * @return Builder
         */
        public Builder tapToRetry(boolean should) {
            this.tapToRetry = should;
            return this;
        }

        /**
         * Load the image while its rendering, this is useful if you want to show previews while its
         * rendering
         * Default: false
         *
         * @param should be enabled
         * @return Builder
         */
        public Builder progressiveRendering(boolean should) {
            this.progressiveRendering = should;
            return this;
        }

        /**
         * Show local thumbnail if present in the exif data
         * Default: false
         *
         * Fresco limitation:
         * This option is supported only for local URIs, and only for images in the JPEG format.
         *
         * @param should show it
         * @return builder
         */
        public Builder localThumbnailPreview(boolean should) {
            this.localThumbnailPreview = should;
            return this;
        }

        /**
         * Use a custom decode options. Create it with ImageDecodeOptionsBuilder.
         * Beware since this handles internal state information. Use at your own risk if needed.
         *
         * @param options for image decoding
         * @return Builder
         */
        public Builder decodeOptions(@NonNull ImageDecodeOptions options) {
            this.decodeOptions = options;
            return this;
        }

        /**
         * Since there are more than one postprocessor and processing methods (see
         * BasePostprocessor and BaseRepeatedPostprocessor) and there are three different
         * processing methods, you should feed the builder with the postprocessor instance already created
         * (instead of us defining a particular method and class for you to process the data)
         *
         * Note: DO NOT override more than one of the bitmap processing methods, this WILL lead to
         * undesired behaviours and is prone to errors
         *
         * Note: Fresco may (in a future, but currently it doesnt) support postprocessing
         * on animations.
         *
         * @param postprocessor instance for images
         * @return Builder
         */
        public Builder postprocessor(@NonNull Postprocessor postprocessor) {
            this.postprocessor = postprocessor;
            return this;
        }

        /**
         * Attach to a view.
         *
         * Note this will handle the loading of the uri. There MUST be (Mandatory) an existent Uri
         * from where to load the image.
         *
         * You can save the returned instance to retreive the data you have used or to explicitly
         * call the callbacks
         *
         * @param view to attach the desired args
         * @return Controller
         */
        public @NonNull FrescoImageController into(@NonNull DraweeView view) {
            if (mUri == null)
                throw new IllegalStateException("Creating controller for drawee with no address to retrieve image from. Forgot to call setUri/setUrl ??");

            return new FrescoImageController(contextRef,
                    mUri, view,
                    listener,
                    resizeOptions, decodeOptions,
                    postprocessor,
                    rotate, tapToRetry, progressiveRendering, localThumbnailPreview,
                    noCache, noDiskCache, noMemmoryCache);
        }

    }

    /**
     * Interface from where callbacks will be dispatched
     */
    public interface Callback {
        void onSuccess();
        void onFailure(@NonNull Throwable t);
    }

}