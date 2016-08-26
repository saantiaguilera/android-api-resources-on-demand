package com.u.dynamic_resources.internal;

/**
 * Created by saguilera on 8/25/16.
 */
public final class Validator {

    public static void checkNullAndThrow(Object context, Object... args) {
        for (Object object : args) {
            if (object == null) {
                throw new NullPointerException("Null object in " + context.getClass().getSimpleName());
            }
        }
    }

    public static boolean checkNull(Object... args) {
        for (Object object : args) {
            if (object == null) {
                return true;
            }
        }

        return false;
    }

}
