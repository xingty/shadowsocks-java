package org.wiyi.ss.utils;

import java.security.SecureRandom;

public class CipherUtils {
    public static byte[] randomBytes(int size) {
        byte[] bytes = new byte[size];
        new SecureRandom().nextBytes(bytes);
        return bytes;
    }
}
