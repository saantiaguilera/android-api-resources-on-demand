package com.u.dynamic_resources.internal;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Pair;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by saguilera on 8/26/16.
 */
class LruCounter {

    private static final String SHARED_PREFERENCES_DIR = LruCounter.class.getName() + "dynamic-resources";

    private WeakReference<Context> context;

    private List<Container> files;
    private long size;
    private long maxSize;

    private DiskCache cache;
    private SharedPreferences sharedPreferences;

    public LruCounter(DiskCache cache, long maxDiskSize) {
        this.context = new WeakReference<>(cache.getContext());
        this.cache = cache;
        this.maxSize = maxDiskSize;
        sharedPreferences = getContext().getSharedPreferences(SHARED_PREFERENCES_DIR, Context.MODE_PRIVATE);

        size = 0;
        files = new ArrayList<>();

        File dir = Files.createDir(getContext());
        if (dir.exists()) {
            for (File child : dir.listFiles()) {
                files.add(new Container(sharedPreferences.getLong(child.getPath(), 0), child.getPath(), child.length()));
                size += child.length();
            }

            Collections.sort(files, new ReverseComparator());
        }
    }

    private Context getContext() {
        return context.get();
    }

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

    class ReverseComparator implements Comparator<Container> {

        @Override
        public int compare(Container lsh, Container lrh) {
            return Long.valueOf(lsh.getTime()).compareTo(lrh.getTime());
        }

    }

    static class Container {

        private long time;
        private @NonNull String path;
        private long size;

        public Container(long time, @NonNull String path, long size) {
            this.time = time;
            this.path = path;
            this.size = size;
        }

        public long getSize() {
            return size;
        }

        public long getTime() {
            return time;
        }

        @NonNull
        public String getPath() {
            return path;
        }

    }

}
