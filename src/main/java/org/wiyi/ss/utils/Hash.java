package org.wiyi.ss.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Hash {
    private static final char[] hexArray = "0123456789abcdef".toCharArray();

    private final MessageDigest digest;

    private Hash(String algorithm) {
        try {
            this.digest = MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static Hash withMD5() {
        return new Hash("MD5");
    }

    public static Hash withSHA1() {
        return new Hash("SHA1");
    }

    public static Hash with(String algorithm) {
        return new Hash(algorithm);
    }

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public String digest(String msg) {
        byte[] data = messageDigest(msg.getBytes());

        return bytesToHex(data);
    }

    public byte[] digest(byte[] data) {
        return messageDigest(data);
    }

    public String md5(String msg) {
        byte[] data = messageDigest(msg.getBytes());

        return bytesToHex(data);
    }

    public byte[] md5(byte[] data) {
        return messageDigest(data);
    }

    private byte[] messageDigest(byte[] data) {
        digest.update(data);
        return digest.digest();
    }
}
