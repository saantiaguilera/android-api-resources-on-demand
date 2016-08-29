package com.u.dynamic_resources.internal;

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

    private static final String DEFAULT_DIR = "dynamic-resources";

    private static final String HASH = "MD5";

    private static final int STRING_RADIX_REPRESENTATION = 16;

    private static final String DEFAULT_EXTENSION = ".jpg";

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

    private static @NonNull String stripExtension(@NonNull String path) {
        try {
            return path.substring(path.lastIndexOf("."));
        } catch (IndexOutOfBoundsException e) {
            return DEFAULT_EXTENSION;
        }
    }

    static @NonNull File createDir(@NonNull Context context) {
        File dir = new File(context.getFilesDir(), DEFAULT_DIR);
        if (!dir.isDirectory()) {
            if (!dir.mkdirs()) {
                throw new IllegalStateException("Couldnt create directory for resources, missing some permissions?");
            }
        }

        return dir;
    }

    public static @NonNull File create(Context context, Uri uri) {
        String url = hash(uri.toString()) + stripExtension(uri.toString());
        return new File(createDir(context), url);
    }

}
