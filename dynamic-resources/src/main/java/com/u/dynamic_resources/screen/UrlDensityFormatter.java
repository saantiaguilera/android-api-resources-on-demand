package com.u.dynamic_resources.screen;

import android.support.annotation.NonNull;

/**
 * Interface for formatting a uri for all the supported densities
 * Created by saguilera on 8/24/16.
 */
public interface UrlDensityFormatter {

    /**
     * Method for getting the particular string the uri has according to each density
     *
     * Eg your server has images with url as
     * http://server.com/image-01-200x200.jpg -> LDPI
     * http://server.com/image-01-400x400.jpg -> MDPI
     * http://server.com/image-01-600x600.jpg -> HDPI
     * http://server.com/image-01-800x800.jpg -> XXXHDPI
     * ...
     *
     * Then you can have a
     *
     * class Example implements UrlDensityFormatter {
     *
     *     //@Override
     *     public String from(@NonNull ScreenDensity myDeviceDensity) {
     *         switch (myDeviceDensity) {
     *             case LDPI:
     *                  return "200x200";
     *             case MDPI:
     *                  return "400x400";
     *             case HDPI:
     *                  return "600x600";
     *             case XXXHDPI:
     *                  return "800x800";
     *         }
     *     }
     *
     * }
     *
     * and the server could send you the image as:
     *
     * String imageUrl = "http://server.com/image-01-%s.jpg";
     *
     * in Pomu just supply a:
     *
     * Pomu.create(...)
     *      ...
     *      .url(imageUrl, new Example())
     *      ...
     *
     * @param density of the device
     * @return String the uri has to have depending of the density the device has
     */
    String from(@NonNull ScreenDensity density);

}
