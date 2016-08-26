package com.u.dynamicresources;

import android.app.Application;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.u.dynamic_resources.core.Pomu;

/**
 * Created by saguilera on 8/25/16.
 */
public class MockApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Fresco.initialize(this);
        Pomu.initialize(this);
    }
}
