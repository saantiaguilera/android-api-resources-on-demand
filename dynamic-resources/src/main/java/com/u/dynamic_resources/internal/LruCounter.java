package com.u.dynamic_resources.internal;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
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
     * Cache utils
     */
    private DiskCache cache;
    private SharedPreferences sharedPreferences;

    /**
     * Constructor for the disk cache with a max size for storing images
     * @param cache
     * @param maxDiskSize
     */
    public LruCounter(DiskCache cache, long maxDiskSize) {
        this.context = new WeakReference<>(cache.getContext());
        this.cache = cache;
        this.maxSize = maxDiskSize;
        sharedPreferences = getContext().getSharedPreferences(SHARED_PREFERENCES_DIR, Context.MODE_PRIVATE);

        size = 0;
        files = new ArrayList<>();

        //Read all the available files and track them
        File dir = Files.createDir(getContext());
        if (dir.exists()) {
            for (File child : dir.listFiles()) {
                files.add(new Container(sharedPreferences.getLong(child.getPath(), 0), child.getPath(), child.length()));
                size += child.length();
            }

            Collections.sort(files, new ReverseComparator());
        }
    }

    /**
     * Get the context with resource access
     * @return Context with resource privileges
     */
    private Context getContext() {
        return context.get();
    }

    /**
     * Evict a key from the cache
     */
    public void evict() {
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

    /**
     * Add a file to the cache
     * @param file to track
     */
    public void add(File file) {
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
