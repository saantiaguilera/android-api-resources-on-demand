package com.u.dynamic_resources.internal.loading;

import android.support.annotation.NonNull;

import java.io.File;

/**
 * Internal class for callbacks of the file request
 * Created by saguilera on 8/25/16.
 */
public interface FileCallback {

    /**
     * When the request was performed successfully
     * @param file where the image download resides
     */
    void onSuccess(@NonNull File file);

    /**
     * When the request failed
     * @param e exception thrown
     */
    void onFailure(@NonNull Exception e);

}
