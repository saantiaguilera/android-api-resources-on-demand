package com.saantiaguilera.dynamic_resources.internal;

/**
 * Validator util class
 * Created by saguilera on 8/25/16.
 */
public final class Validator {

    /**
     * Check the args arent null, and throw a NPE if one of them is
     * @param context context caller
     * @param args to check against null
     */
    public static void checkNullAndThrow(Object context, Object... args) {
        for (Object object : args) {
            if (object == null) {
                throw new NullPointerException("Null object in " + context.getClass().getSimpleName());
            }
        }
    }

}
