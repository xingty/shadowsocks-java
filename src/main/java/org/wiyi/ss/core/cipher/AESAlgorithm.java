package org.wiyi.ss.core.cipher;

import org.wiyi.ss.core.exception.SSCipherException;

public class AESAlgorithm {
    /**
     * algorithm name
     */
    private String name;

    /**
     * modes such as CFB and OFB
     */
    private String mode;

    private String padding;

    private int keyLength;

    public static AESAlgorithm parse(String name) {
        AESAlgorithm a = new AESAlgorithm();
        String[] arr = name.split("-");
        if (arr.length != 3) {
            throw new SSCipherException("invalid algorithm name: " + name);
        }

        a.name = arr[0].toUpperCase();
        a.keyLength = Integer.parseInt(arr[1]);
        if (a.keyLength != 128 && a.keyLength != 192 && a.keyLength != 256) {
            throw new SSCipherException("invalid aes keyLen: " + a.keyLength);
        }

        a.mode = arr[2].toUpperCase();

        if ("CFB".equals(a.mode) || "CTR".equals(a.mode) || "OFB".equals(a.mode)) {
            a.padding = "NoPadding";
        } else {
            a.padding = "PKCS5Padding";
        }

        return a;
    }

    @Override
    public String toString() {
        return String.format("%s-%s-%s",name,mode,padding);
    }

    public String getName() {
        return name;
    }

    public String getMode() {
        return mode;
    }

    public String getPadding() {
        return padding;
    }

    public int getKeyLength() {
        return keyLength;
    }
}
