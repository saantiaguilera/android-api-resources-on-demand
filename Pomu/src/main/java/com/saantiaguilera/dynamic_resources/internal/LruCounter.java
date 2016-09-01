package com.saantiaguilera.dynamic_resources.internal;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Helper class for doin LRU in the disk cache.
 *
 * Created by saguilera on 8/26/16.
 */
class LruCounter {

    //Shared preferences directory to count
    private static final String SHARED_PREFERENCES_DIR = LruCounter.class.getName() + "dynamic-resources";

    private WeakReference<Context> context;

    /**
     * Files to keep track of the LRU. Since there are persisted files prior to this creation
     * (from a previous application run lets say) its highly recommended to create this (create the cache)
     * in the application or create, else this can slow down the performance of the app
     *
     * (because it will read all the current persisted files and track them on runtime, on the
     * main thread).
     * This could be done in a worker thread, but if its too slow, subsequent requests might not use this cache,
     * because of race conditions (we could also use a semaphore, but I find this approach way too prone to errors).
     *
     * To avoid high memory usage, instead of tracking the whole file, we just track its sensitive data
     */
    private List<Container> files;
    private long size;
    private long maxSize;

    /**
     * Variables for the initialization logic
     */
    private final Object lock = new Object();
    private boolean initialized = false;
    private static final int MAX_TIME_IDLE = 2000;

    /**
     * Cache utils
     */
    private DiskCache cache;
    private SharedPreferences sharedPreferences;

    /**
     * Constructor for the disk cache with a max size for storing images
     * @param cache DiskCache instance
     * @param maxDiskSize max disk size to use for persisting data
     */
    public LruCounter(DiskCache cache, long maxDiskSize) {
        this.context = new WeakReference<>(cache.getContext());
        this.cache = cache;
        this.maxSize = maxDiskSize;
        sharedPreferences = getContext().getSharedPreferences(SHARED_PREFERENCES_DIR, Context.MODE_PRIVATE);

        size = 0;
        files = new ArrayList<>();

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                synchronized (lock) {
                    //Read all the available files and track them
                    File dir = Files.createDir(getContext());
                    if (dir.exists()) {
                        for (File child : dir.listFiles()) {
                            files.add(new Container(sharedPreferences.getLong(child.getPath(), 0), child.getPath(), child.length()));
                            size += child.length();
                        }
                    }

                    Collections.sort(files, new ReverseComparator());

                    initialized = true;
                    lock.notifyAll();
                }
            }
        });
    }

    /**
     * Get the context with resource access
     * @return Context with resource privileges
     */
    private Context getContext() {
        return context.get();
    }

    /**
     * This method might be complex.
     * In ALL cases, this will just return true. Because we initialize Pomu, and the cache will load
     * and by the time the first Pomu image gets loaded, this has already been initialized long time ago.
     *
     * If we want in the application to download images, this images will be downloaded the first time the app
     * is run. So this cache will also be initialized asap, without having to wait. (+ the image takes some time
     * to get downloaded remember)
     *
     * But what if we already have our cache filled, and we upload an update with MORE images to download in
     * the application, and the user updates his app. The cache will surely take some time (~600ms) to load
     * and the new images need to be downloaded.
     *
     * If we just returned false, we wouldnt be tracking in that first time those images (not a big deal), but
     * still thats considered a bug. So we make the main thread idle (its in the application first time, so its part of the cold
     * start) for 2 sec max until it gets loaded. If it does, it was just a first time cold start, else it will return
     * false and we wont be tracking in that first time the downloaded images.
     *
     * Remember also that the new images have to be downloaded from the net, so its highly unprobable to happen.
     * But better safe than worry.
     *
     * @return if its initialized or not
     */
    private boolean isInitialized() {
        if (!initialized) {
            try {
                synchronized (lock) {
                    lock.wait(MAX_TIME_IDLE);
                }
            } catch (InterruptedException e) {
                //Make it return the initialized state.
            }
        }

        return initialized;
    }

    /**
     * Evict a key from the cache
     */
    public void evict() {
        if (isInitialized()) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            boolean changed = false;

            while (size > maxSize && !files.isEmpty()) {
                Container container = files.get(0);

                size -= container.getSize();

                cache.remove(Uri.parse(container.getPath()));
                files.remove(0);
                editor.remove(container.getPath());

                changed = true;
            }

            if (changed) {
                editor.apply();
            }
        }
    }

    /**
     * Add a file to the cache
     * @param file to track
     */
    public void add(File file) {
        if (isInitialized()) {
            sharedPreferences.edit()
                    .putLong(file.getPath(), System.currentTimeMillis())
                    .apply();

            Container existing = null;
            for (Container container : files) {
                if (container.getPath().contentEquals(file.getPath())) {
                    existing = container;
                    break;
                }
            }

            if (existing != null) {
                files.remove(existing);
            } else {
                size += file.length();
            }

            files.add(new Container(System.currentTimeMillis(), file.getPath(), file.length()));

            Collections.sort(files, new ReverseComparator());
        }
    }

    /**
     * Comparator class to sort from lowest size to highest size of files
     */
    class ReverseComparator implements Comparator<Container> {

        @Override
        public int compare(Container lsh, Container lrh) {
            return Long.valueOf(lsh.getTime()).compareTo(lrh.getTime());
        }

    }

    /**
     * DTO Class for tracking files in the cache
     */
    static class Container {

        private long time;
        private @NonNull String path;
        private long size;

        /**
         * Constructor
         * @param time when the file was last used/accessed
         * @param path absolute path of the file
         * @param size size of the file in bytes
         */
        public Container(long time, @NonNull String path, long size) {
            this.time = time;
            this.path = path;
            this.size = size;
        }

        /**
         * Getter
         * @return size in bytes
         */
        public long getSize() {
            return size;
        }

        /**
         * Getter
         * @return last accessed time
         */
        public long getTime() {
            return time;
        }

        /**
         * Getter
         * @return absolute path of the file
         */
        @NonNull
        public String getPath() {
            return path;
        }

    }

}
