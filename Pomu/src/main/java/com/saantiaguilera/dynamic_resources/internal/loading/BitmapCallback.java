package com.saantiaguilera.dynamic_resources.internal.loading;

import android.support.annotation.NonNull;

/**
 * Class developers should use to receive callbacks about the state of a request
 * Created by saguilera on 8/24/16.
 */
public interface BitmapCallback {

    /**
     * Called when the request was successful
     */
    void onSuccess();

    /**
     * Called when the request failed
     * @param throwable exception thrown
     */
    void onFailure(@NonNull Throwable throwable);

}
