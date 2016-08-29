package com.u.dynamic_resources.screen;

import android.content.res.Resources;
import android.util.DisplayMetrics;

/**
 * Screen density enum for the given android screens
 *
 * Created by saguilera on 8/24/16.
 */
public enum ScreenDensity {

    /**
     * Supported densities
     */
    LDPI(ScreenDensity.DENSITY_L),
    MDPI(ScreenDensity.DENSITY_M),
    HDPI(ScreenDensity.DENSITY_H),
    XHDPI(ScreenDensity.DENSITY_XH),
    XXHDPI(ScreenDensity.DENSITY_XXH),
    XXXHDPI(ScreenDensity.DENSITY_XXXH),
    TV(ScreenDensity.DENSITY_TV);

    private final static int DENSITY_L = DisplayMetrics.DENSITY_LOW;
    private final static int DENSITY_M = DisplayMetrics.DENSITY_MEDIUM;
    private final static int DENSITY_H = DisplayMetrics.DENSITY_HIGH;
    private final static int DENSITY_XH = DisplayMetrics.DENSITY_XHIGH;
    private final static int DENSITY_XXH = 480; //Very unlikely they will change
    private final static int DENSITY_XXXH = 640; //They require api 16 min...
    private final static int DENSITY_TV = 213; //This requires api 13 min...

    private int density;

    /**
     * Package access constructor for the enums
     * @param density of the screen
     */
    ScreenDensity(int density) {
        this.density = density;
    }

    /**
     * Getter of the density the enum has
     * @return density of the enum
     */
    public int getDensity() {
        return density;
    }

    /**
     * Get the density of the device
     * @param resources to fetch the current density
     * @return ScreenDensity with the device density
     */
    public static ScreenDensity get(Resources resources) {
        int densityDpi = resources.getDisplayMetrics().densityDpi;

        for (ScreenDensity density : ScreenDensity.values()) {
            if (density.getDensity() == densityDpi) {
                return density;
            }
        }

        return MDPI;
    }

}
