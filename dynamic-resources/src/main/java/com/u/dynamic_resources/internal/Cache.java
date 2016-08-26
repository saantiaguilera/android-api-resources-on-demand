package com.u.dynamic_resources.internal;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;
import java.io.InputStream;

/**
 * Created by saguilera on 8/26/16.
 */
public interface Cache {

    File put(@NonNull Uri key, @NonNull InputStream data) throws Exception;

    boolean contains(@NonNull Uri key);

    @Nullable File get(@NonNull Uri key);

    boolean remove(@NonNull Uri key);

    void clear();

}
