package com.u.dynamic_resources.internal;

import android.content.Context;
import android.net.Uri;

import java.io.File;
import java.math.BigInteger;
import java.security.MessageDigest;

/**
 * Created by saguilera on 8/25/16.
 */
final class Files {

    private static final String HASH = "MD5";

    private static final int STRING_RADIX_REPRESENTATION = 16;

    private static final String DEFAULT_EXTENSION = ".jpg";

    private static String hash(String name) {
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

    private static String stripExtension(String path) {
        try {
            return path.substring(path.lastIndexOf("."));
        } catch (IndexOutOfBoundsException e) {
            return DEFAULT_EXTENSION;
        }
    }

    public static File create(Context context, Uri uri) {
        String url = hash(uri.toString()) + stripExtension(uri.toString());
        return new File(context.getFilesDir(), url);
    }

}
