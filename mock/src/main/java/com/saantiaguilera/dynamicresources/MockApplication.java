package com.saantiaguilera.dynamicresources;

import android.app.Application;
import android.os.StrictMode;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.saantiaguilera.dynamic_resources.core.Pomu;

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

        Pomu.create(this)
                .url("http://pre00.deviantart.net/9c0a/th/pre/i/2014/083/f/4/metalic_texture_hd_3000x3000_by_luisbc-d7bgzz8.png")
                .url("http://www.space-images.com/photos/nebula/M42_Orion/Orion_Nebula_3000x3000.jpg")
                .url("http://www.curezone.org/upload/Members/New02/nasa1R3107_1000x1000.jpg")
                .url("http://www.curezone.org/upload/Members/New02/nasa1R3107_1000x1000.jpg")
                .url("http://farm3.static.flickr.com/2402/2216325948_977929bf5d_o.png")
                .url("http://sectools.org/flags/mouse-30x30.png")
                .url("https://classifieds.harvardmagazine.com/sites/all/themes/hm/images/contact_images/phone_30x30.png")
                .url("https://s3.amazonaws.com/sio-cdn/images/footer/icon/icon-twitter-30x30.png")
                .url("http://icons.iconarchive.com/icons/hamzasaleem/stock/128/Launchpad-icon.png")
                .url("http://www.newsread.in/wp-content/uploads/2016/06/Images-2.png")
                .url("http://www.newsread.in/wp-content/uploads/2016/06/Images-4.jpg")
                .get();
    }
}
