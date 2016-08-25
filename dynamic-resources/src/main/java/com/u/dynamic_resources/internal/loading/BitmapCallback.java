package com.u.dynamic_resources.internal.loading;

import android.graphics.Bitmap;

/**
 * Created by saguilera on 8/24/16.
 */
public interface BitmapCallback {

    void onSuccess(Bitmap bitmap);

    void onFailure(Throwable throwable);

}
