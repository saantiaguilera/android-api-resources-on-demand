package com.u.dynamicresources;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.u.dynamic_resources.ScreenDensity;

/**
 * Created by saguilera on 8/24/16.
 */
public class MockActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.w(this.getClass().getSimpleName(), "screen density : " + ScreenDensity.get(this.getResources()).toString());
    }

}
