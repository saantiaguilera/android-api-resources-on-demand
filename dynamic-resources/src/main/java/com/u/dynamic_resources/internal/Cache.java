package com.u.dynamic_resources.internal;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;
import java.io.InputStream;

/**
 * Cache interface class. Developers should implement this if they want to supply a custom cache
 *
 * Created by saguilera on 8/26/16.
 */
public interface Cache {

    /**
     * Put new data in the cache with a given key.
     * @param key key which will be used in every other transaction to retreive the data
     * @param data byte[] in a buffer of the image
     * @return File a file with the image data
     * @throws Exception if there was a problem writing the file or caching the image
     */
    File put(@NonNull Uri key, @NonNull InputStream data) throws Exception;

    /**
     * Method to know if a key is in the cache
     * @param key key which will be used in every other transaction
     * @return true if the key exists, false otherwise
     */
    boolean contains(@NonNull Uri key);

    /**
     * Get the file asociated to a given key.
     * @param key key which will be used in every other transaction
     * @return File if the key exists, null otherwise
     */
    @Nullable File get(@NonNull Uri key);

    /**
     * Remove a key from the cache
     * @param key key which will be used in every other transaction
     * @return true if was removed, false otherwise
     */
    boolean remove(@NonNull Uri key);

    /**
     * Clear the cache data
     */
    void clear();

}
