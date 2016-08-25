package com.u.dynamic_resources.internal;

/**
 * Created by saguilera on 8/25/16.
 */
class Validator {

    public static void checkNull(Object context, Object... args) {
        for (Object object : args) {
            if (object == null) {
                throw new NullPointerException("Null object in " + context.getClass().getSimpleName());
            }
        }
    }

}
