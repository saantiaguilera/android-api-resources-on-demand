package com.u.dynamic_resources.internal;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Pair;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by saguilera on 8/26/16.
 */
class LruCounter {

    private static final int DISK_SIZE = 1024 * 1024 * 15; // 15MB

    private static final String SHARED_PREFERENCES_DIR = LruCounter.class.getName() + "dynamic-resources";

    private Context context;

    private List<Pair<Long, File>> files;
    private int size;

    private DiskCache cache;
    private SharedPreferences sharedPreferences;

    public LruCounter(DiskCache cache) {
        this.context = cache.getContext();
        this.cache = cache;
        sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_DIR, Context.MODE_PRIVATE);

        size = 0;
        files = new ArrayList<>();

        File dir = Files.createDir(context);
        if (dir.exists()) {
            for (File child : dir.listFiles()) {
                files.add(new Pair<>(sharedPreferences.getLong(child.getPath(), 0), child));
                size += child.length();
            }

            Collections.sort(files, Collections.reverseOrder());
        }
    }

    public void evict() {
        File dir = Files.createDir(context);
        if (dir.exists()) {
            SharedPreferences.Editor editor = sharedPreferences.edit();

            while (size > DISK_SIZE && !files.isEmpty()) {
                File file = files.get(0).second;

                size -= file.length();

                cache.remove(Uri.parse(file.getPath()));
                files.remove(0);
                editor.remove(file.getPath());
            }

            editor.apply();
        }
    }

    public void add(File file) {
        sharedPreferences.edit()
                .putLong(file.getPath(), System.currentTimeMillis())
                .apply();


        Pair<Long, File> existing = null;
        for (Pair<Long, File> f : files) {
            if (f.second.getPath().contentEquals(file.getPath())) {
                existing = f;
                break;
            }
        }

        if (existing != null) {
            files.remove(existing);
        }

        files.add(new Pair<>(System.currentTimeMillis(), file));
    }

}
