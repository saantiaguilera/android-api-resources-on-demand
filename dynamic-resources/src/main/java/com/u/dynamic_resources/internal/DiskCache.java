package com.u.dynamic_resources.internal;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * Created by saguilera on 8/26/16.
 */
public class DiskCache implements Cache {

    private static final int BUFFER_SIZE = 1024;

    private Context context;
    private LruCounter lruCounter;

    public DiskCache(Context context) {
        this.context = context;
        this.lruCounter = new LruCounter(this);
    }

    Context getContext() {
        return context;
    }

    @WorkerThread
    private boolean write(File output, InputStream data) throws Exception {
        FileOutputStream fos = null;

        try {
            if (output.exists()) {
                return true;
            }

            fos = new FileOutputStream(output);
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead = 0;
            while ((bytesRead = data.read(buffer, 0, buffer.length)) >= 0) {
                fos.write(buffer, 0, bytesRead);
            }

            fos.flush();
        } finally {
            if (data != null) {
                data.close();
            }

            if (fos != null) {
                fos.close();
            }
        }

        return true;
    }

    @Override
    public void put(@NonNull Uri key, @NonNull InputStream data) throws Exception {
        File file = Files.create(context, key);
        write(file, data);

        lruCounter.add(file);
        lruCounter.evict();
    }

    @Override
    public boolean contains(@NonNull Uri key) {
        return Files.create(context, key).exists();
    }

    @Override
    public @Nullable File get(@NonNull Uri key) {
        File file = Files.create(context, key);
        lruCounter.add(file);
        return file;
    }

    @Override
    public boolean remove(@NonNull Uri key) {
        //Consider the key already hashed (its a file, since he knows what he wants to remove)
        File file = new File(key.toString());

        return !file.exists() || file.delete();
    }

    @Override
    public void clear() {
        deleteAllIn(Files.createDir(context));
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void deleteAllIn(File file) {
        if (file.isDirectory())
            for (File child : file.listFiles())
                deleteAllIn(child);

        file.delete();
    }

}
