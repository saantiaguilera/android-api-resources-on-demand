package com.u.dynamic_resources.internal.loading;

import android.support.annotation.NonNull;

/**
 * Created by saguilera on 8/24/16.
 */
public interface BitmapCallback {

    void onSuccess();

    void onFailure(@NonNull Throwable throwable);

}
