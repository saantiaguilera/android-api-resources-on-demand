package com.u.dynamicresources;

import android.app.Application;
import android.os.StrictMode;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.u.dynamic_resources.core.Pomu;

/**
 * Created by saguilera on 8/25/16.
 */
public class MockApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectAll()// or .detectAll() for all detectable problems
                .penaltyLog()
                .build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build());

        Fresco.initialize(this);
        Pomu.initialize(this);
    }
}
