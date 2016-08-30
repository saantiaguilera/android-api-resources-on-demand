package com.saantiaguilera.dynamic_resources.internal;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;

import java.io.File;
import java.math.BigInteger;
import java.security.MessageDigest;

/**
 * Created by saguilera on 8/25/16.
 */
final class Files {

    //Default dir where the files will be stored. This isnt hashed.
    private static final String DEFAULT_DIR = "dynamic-resources";

    //Hash mode
    private static final String HASH = "MD5";

    //Radix for the hash
    private static final int STRING_RADIX_REPRESENTATION = 16;

    //Default extension in case image doesnt has one
    private static final String DEFAULT_EXTENSION = ".jpg";

    /**
     * Hash a string using HASH mode.
     * @param name string to hash
     * @return string with the hashed name
     */
    private static @NonNull String hash(@NonNull String name) {
        try {
            MessageDigest m = MessageDigest.getInstance(HASH);
            m.update(name.getBytes());
            byte[] digest = m.digest();
            BigInteger bigInt = new BigInteger(1, digest);
            return bigInt.toString(STRING_RADIX_REPRESENTATION);
        } catch (Exception e) {
            return name;
        }
    }

    /**
     * Strip the extension from a path
     * @param path to get the extension from
     * @return string with the extension of the path
     */
    private static @NonNull String stripExtension(@NonNull String path) {
        try {
            return path.substring(path.lastIndexOf("."));
        } catch (IndexOutOfBoundsException e) {
            return DEFAULT_EXTENSION;
        }
    }

    /**
     * Package access method to create the directory were the resources will lay
     * @param context with resources access
     * @return File pointing to the dir
     */
    static @NonNull File createDir(@NonNull Context context) {
        File dir = new File(context.getFilesDir(), DEFAULT_DIR);
        if (!dir.isDirectory()) {
            if (!dir.mkdirs()) {
                throw new IllegalStateException("Couldnt create directory for resources, missing some permissions?");
            }
        }

        return dir;
    }

    /**
     * Create a file for the given uri. Please be careful the name will be hashed, so use it carefully.
     *
     * @param context with resource access
     * @param uri for the given file
     * @return File for the given uri
     */
    public static @NonNull File create(Context context, Uri uri) {
        String url = hash(uri.toString()) + stripExtension(uri.toString());
        return new File(createDir(context), url);
    }

}
