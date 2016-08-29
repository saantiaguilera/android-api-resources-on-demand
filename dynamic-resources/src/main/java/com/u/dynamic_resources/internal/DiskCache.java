package com.u.dynamic_resources.internal;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;

/**
 * Custom cache that uses as default Pomu.
 * If users want to still use this, but change the maximum disk size available for the cache,
 * they can supply  in a custom configurations this class using the 2 params constructor
 *
 * Created by saguilera on 8/26/16.
 */
public class DiskCache implements Cache {

    //Maximum disk size to be used by the cache
    private static final int DEFAULT_DISK_SIZE = 1024 * 1024 * 15; // 15MB

    //Buffer size for writing files
    private static final int BUFFER_SIZE = 1024;

    private WeakReference<Context> context;
    private LruCounter lruCounter;

    /**
     * Default constructor with DEFAULT_DISK_CACHE as max disk size.
     * @param context with resource access
     */
    public DiskCache(Context context) {
        this(context, DEFAULT_DISK_SIZE);
    }

    /**
     * Constructor with a custom disk size to store more (or less).
     * @param context with resource access
     * @param maxDiskSize the cache will use to store images, or either clean up.
     */
    public DiskCache(Context context, long maxDiskSize) {
        this.context = new WeakReference<>(context);
        this.lruCounter = new LruCounter(this, maxDiskSize);
    }

    /**
     * Package access Context getter
     * @return Context with resource access
     */
    Context getContext() {
        return context.get();
    }

    /**
     * Method to write an input stream in a file. This method should always be run in a background
     * worker.
     *
     * @param output file were the data will be written
     * @param data of the image
     * @return true if it was successfully written, false otherwise
     * @throws Exception if something wrong happened
     */
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

    /**
     * Cache the data with the given key.
     * This method uses the Files to create a hashed file of the key and not use the key
     * directly (for security measures).
     *
     * @param key key which will be used in every other transaction to retreive the data
     * @param data byte[] in a buffer of the image
     * @return File with the data stored for the given key
     * @throws Exception
     */
    @Override
    @WorkerThread
    public File put(@NonNull Uri key, @NonNull InputStream data) throws Exception {
        File file = Files.create(getContext(), key);
        write(file, data);

        lruCounter.add(file);
        lruCounter.evict();

        return file;
    }

    /**
     * Method for knowing if a key is or not in the cache
     * @param key key which will be used in every other transaction
     * @return true if exists, false otherwise.
     */
    @Override
    public boolean contains(@NonNull Uri key) {
        return Files.create(getContext(), key).exists();
    }

    /**
     * Get the File according to the given key.
     * @param key key which will be used in every other transaction
     * @return File if exists, null otherwise
     */
    @Override
    public @Nullable File get(@NonNull Uri key) {
        File file = Files.create(getContext(), key);

        if (file.exists()) {
            lruCounter.add(file);
            return file;
        }

        return null;
    }

    /**
     * Remove the given key from the cache
     * @param key key which will be used in every other transaction
     * @return true if removed, false otherwise
     */
    @Override
    @WorkerThread
    public boolean remove(@NonNull Uri key) {
        //Consider the key already hashed (its a file, since he knows what he wants to remove)
        File file = new File(key.toString());

        return !file.exists() || file.delete();
    }

    /**
     * Wipe all data from the cache
     */
    @Override
    @WorkerThread
    public void clear() {
        deleteAllIn(Files.createDir(getContext()));
    }

    /**
     * Recursive method for deleting files in inner dirs too.
     * @param file to delete or if its a dir, recursively call this
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    @WorkerThread
    private void deleteAllIn(File file) {
        if (file.isDirectory())
            for (File child : file.listFiles())
                deleteAllIn(child);

        file.delete();
    }

}
