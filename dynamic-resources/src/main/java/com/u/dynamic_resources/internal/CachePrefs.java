package com.u.dynamic_resources.internal;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.WorkerThread;
import android.text.method.HideReturnsTransformationMethod;
import android.util.Pair;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by saguilera on 8/26/16.
 */
public class CachePrefs {

    private static final int DISK_SIZE = 1024 * 1024 * 15; // 15MB

    private static final String SHARED_PREFERENCES_DIR = CachePrefs.class.getName() + "dynamic-resources";

    public static void evictIfNeeded(Context context, Cache cache) {
        File dir = Files.createDir(context);
        if (dir.exists()) {
            long length = 0;
            List<Pair<Long, File>> files = new ArrayList<>();
            SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_DIR, Context.MODE_PRIVATE);

            for (File child : dir.listFiles()) {
                length += child.length();

                files.add(new Pair<>(sharedPreferences.getLong(child.getPath(), 0), child));
            }

            Collections.sort(files, Collections.reverseOrder());

            SharedPreferences.Editor editor = sharedPreferences.edit();

            while (length > DISK_SIZE && !files.isEmpty()) {
                File file = files.get(0).second;

                length -= file.length();

                cache.remove(Uri.parse(file.getPath()));
                files.remove(0);
                editor.remove(file.getPath());
            }

            editor.apply();
        }
    }
    
    public static void mark(Context context, String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_DIR, Context.MODE_PRIVATE);
        sharedPreferences.edit()
                .putLong(key, System.currentTimeMillis())
                .apply();
    }

}
