package com.u.dynamic_resources.internal.loading;

import android.support.annotation.NonNull;

import java.io.File;

/**
 * Created by saguilera on 8/25/16.
 */
public interface FileCallback {

    void onFailure(@NonNull Exception e);

    void onSuccess(@NonNull File file);

}
