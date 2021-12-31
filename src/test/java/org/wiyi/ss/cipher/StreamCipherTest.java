package org.wiyi.ss.cipher;

import org.junit.Assert;
import org.junit.Test;
import org.wiyi.ss.core.cipher.SSCipher;
import org.wiyi.ss.core.cipher.impl.AESStreamCipher;
import org.wiyi.ss.core.cipher.impl.Rc4Md5Cipher;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class StreamCipherTest {
    private static final byte[] SOURCE = "123456".getBytes(StandardCharsets.UTF_8);

    @Test
    public void testRC4MD5() {
        Rc4Md5Cipher cipher = new Rc4Md5Cipher("123456");
        doTest(cipher);
    }

    @Test
    public void testAES() {
        AESStreamCipher a1 = new AESStreamCipher("123456","aes-128-cfb");
        AESStreamCipher a2 = new AESStreamCipher("123456","aes-256-cfb");
        AESStreamCipher a3 = new AESStreamCipher("123456","aes-128-ctr");
        AESStreamCipher a4 = new AESStreamCipher("123456","aes-256-cfb");
        doTest(a1);
        doTest(a2);
        doTest(a3);
        doTest(a4);
    }

    private void doTest(SSCipher cipher) {
        System.out.println("plaintext: " + Arrays.toString(SOURCE));
        byte[] ciphertext = cipher.encrypt(SOURCE);
        System.out.println("ciphertext: " + Arrays.toString(ciphertext));
        byte[] p1 = cipher.decrypt(ciphertext);

        Assert.assertArrayEquals(SOURCE,p1);
    }
}
