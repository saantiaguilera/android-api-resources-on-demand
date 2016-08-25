package com.u.dynamicresources;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.ImageView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.u.dynamic_resources.core.Pomu;
import com.u.dynamic_resources.internal.fresco.FrescoImageController;
import com.u.dynamic_resources.internal.loading.BitmapCallback;
import com.u.dynamic_resources.screen.ScreenDensity;
import com.u.dynamic_resources.screen.UrlDensityFormatter;

/**
 * Created by saguilera on 8/24/16.
 */
public class MockActivity extends Activity {

    private static final String IMAGEN_ESTUFA = "http://www.estufas1.com.mx/images/e_ge/estufa_piso_ge_EG3094DBI.jpg";
    private static final String IMAGEN_CHIMENEA_FORMATTED = "http://www.okdecoracion.com/images/chimenea%s.jpg"; //1 al 6

    private UrlDensityFormatter urlFormatter;
    private BitmapCallback bitmapCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_mock);

        Log.w(this.getClass().getSimpleName(), "screen density : " + ScreenDensity.get(this.getResources()).toString());


        bitmapCallback = new BitmapCallback() {
            @Override
            public void onSuccess() {
                Log.w("Sansa", "onSuccess");
            }

            @Override
            public void onFailure(@NonNull Throwable throwable) {
                Log.w("Sansa", "onFailure");
                throwable.printStackTrace();
            }
        };

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
                .parse(IMAGEN_ESTUFA)
                .callback(bitmapCallback)
                .into((ImageView) findViewById(R.id.activity_mock_image_1));

        //Static image with fresco with custom controller
        Pomu.create(this)
                .parse(IMAGEN_ESTUFA)
                .callback(bitmapCallback)
                .controller(FrescoImageController.create(this)
                        .autoRotate(true)
                        .resize(100,100)
                        .progressiveRendering(true)
                ).into((ImageView) findViewById(R.id.activity_mock_image_2));

        //Dynamic image with fresco without custom controller
        Pomu.create(this)
                .parse(IMAGEN_CHIMENEA_FORMATTED, urlFormatter)
                .callback(bitmapCallback)
                .into((ImageView) findViewById(R.id.activity_mock_image_3));


        //Static image with imageview
        Pomu.create(this)
                .parse(IMAGEN_ESTUFA)
                .callback(bitmapCallback)
                .into((ImageView) findViewById(R.id.activity_mock_image_4));


        //Dynamic image with imageview
        Pomu.create(this)
                .parse(IMAGEN_CHIMENEA_FORMATTED, urlFormatter)
                .callback(bitmapCallback)
                .into((ImageView) findViewById(R.id.activity_mock_image_5));
    }
}
