package com.u.dynamic_resources.internal.loading;

import java.io.File;

/**
 * Created by saguilera on 8/25/16.
 */
public interface FileCallback {

    void onFailure(Exception e);

    void onSuccess(File file);

}
