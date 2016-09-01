package com.saantiaguilera.dynamicresources;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.ImageView;

import com.saantiaguilera.dynamic_resources.core.Pomu;
import com.saantiaguilera.dynamic_resources.internal.fresco.FrescoImageController;
import com.saantiaguilera.dynamic_resources.internal.loading.BitmapCallback;
import com.saantiaguilera.dynamic_resources.screen.ScreenDensity;
import com.saantiaguilera.dynamic_resources.screen.UrlDensityFormatter;

/**
 * Activity class for testing
 * Created by saguilera on 8/24/16.
 */
public class MockActivity extends Activity {

    //Three images for testing purposes
    private static final String EXAMPLE_IMAGE_1 = "http://www.estufas1.com.mx/images/e_ge/estufa_piso_ge_EG3094DBI.jpg";
    private static final String EXAMPLE_IMAGE_2 = "http://i.stack.imgur.com/Pryyn.png";
    private static final String EXAMPLE_IMAGE_3 = "https://d319i1jp2i9xq6.cloudfront.net/upload/images/31383/31383_p.jpg";

    //This one goes from 1 to 6.
    private static final String EXAMPLE_IMAGE_4_FORMATTED = "http://www.okdecoracion.com/images/chimenea%s.jpg";

    private UrlDensityFormatter urlFormatter;
    private BitmapCallback bitmapCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_mock);

        Log.w(this.getClass().getSimpleName(), "screen density : " + ScreenDensity.get(this.getResources()).toString());

        //Anonymous class for callbacks
        bitmapCallback = new BitmapCallback() {
            @Override
            public void onSuccess() {
                Log.w("Sansa", "onSuccess");
                System.gc();
            }

            @Override
            public void onFailure(@NonNull Throwable throwable) {
                Log.w("Sansa", "onFailure");
                throwable.printStackTrace();
            }
        };

        //Anonymous class for url formatting in the dynamic urls
        urlFormatter = new UrlDensityFormatter() {
            @Override
            public String from(@NonNull ScreenDensity density) {
                switch (density) {
                    case HDPI:
                        return "2";
                    case XXXHDPI:
                        return "6";
                    default:
                        return "1";
                }
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();

        //Static image with fresco without custom controller
        Pomu.create(this)
                .url(EXAMPLE_IMAGE_1)
                .callback(bitmapCallback)
                .into((ImageView) findViewById(R.id.activity_mock_image_1));

        //Static image with fresco with custom controller
        Pomu.create(this)
                .url(EXAMPLE_IMAGE_2)
                .callback(bitmapCallback)
                .controller(FrescoImageController.create(this)
                        .autoRotate(true)
                        .resize(100,100)
                        .progressiveRendering(true)
                ).into((ImageView) findViewById(R.id.activity_mock_image_2));

        //Dynamic image with fresco without custom controller
        Pomu.create(this)
                .url(EXAMPLE_IMAGE_4_FORMATTED, urlFormatter)
                .callback(bitmapCallback)
                .into((ImageView) findViewById(R.id.activity_mock_image_3));

        //Dynamic image with imageview
        Pomu.create(this)
                .url(EXAMPLE_IMAGE_4_FORMATTED, urlFormatter)
                .callback(bitmapCallback)
                .into((ImageView) findViewById(R.id.activity_mock_image_5));

        //Static image with imageview
        Pomu.create(this)
                .url(EXAMPLE_IMAGE_3)
                .callback(bitmapCallback)
                .into((ImageView) findViewById(R.id.activity_mock_image_4));
    }
}
