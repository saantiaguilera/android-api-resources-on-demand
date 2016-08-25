package com.u.dynamicresources;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.ImageView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.u.dynamic_resources.core.Pomu;
import com.u.dynamic_resources.internal.loading.BitmapCallback;
import com.u.dynamic_resources.screen.ScreenDensity;

/**
 * Created by saguilera on 8/24/16.
 */
public class MockActivity extends Activity {

    private static final String IMAGEN_AUTO = "http://www.autoeurope.com/images-fc/StandardImage/lamborghini-aventador-auto-europe1.jpg";
    private static final String IMAGEN_ESTUFA = "http://www.estufas1.com.mx/images/e_ge/estufa_piso_ge_EG3094DBI.jpg";
    private static final String IMAGEN_CHIMENEA_FORMATTED = "http://www.okdecoracion.com/images/chimenea%s.jpg"; //1 al 6

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_mock);

        Log.w(this.getClass().getSimpleName(), "screen density : " + ScreenDensity.get(this.getResources()).toString());

        Pomu.create(this)
                .parse(IMAGEN_ESTUFA)
                .callback(new BitmapCallback() {
                    @Override
                    public void onSuccess() {
                        Log.w("Sansa", "onSuccess");
                    }

                    @Override
                    public void onFailure(@NonNull Throwable throwable) {
                        Log.w("Sansa", "onFailure");
                        throwable.printStackTrace();
                    }
                }).into((ImageView) findViewById(R.id.activity_mock_image_1));
    }

}
