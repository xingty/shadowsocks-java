package org.wiyi.ss.cipher;

import org.junit.Assert;
import org.junit.Test;
import org.wiyi.ss.core.cipher.SSAEADCipher;
import org.wiyi.ss.core.cipher.impl.AESGCMCipher;
import org.wiyi.ss.core.cipher.impl.ChaCha20Poly1305Cipher;
import org.wiyi.ss.utils.ArrayUtils;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Random;

public class AEADCipherTest {
    private static final byte[] SOURCE = new byte[]{1, 112, 80, -8, 124, 0, 80, 71, 69, 84, 32, 47, 32, 72, 84, 84, 80, 47, 49, 46, 49, 13, 10, 72, 111, 115, 116, 58, 32, 97, 46, 98, 97, 105, 100, 117, 46, 99, 111, 109, 13, 10, 85, 115, 101, 114, 45, 65, 103, 101, 110, 116, 58, 32, 99, 117, 114, 108, 47, 55, 46, 54, 52, 46, 49, 13, 10, 65, 99, 99, 101, 112, 116, 58, 32, 42, 47, 42, 13, 10, 13, 10};
    private static final byte[] SOURCE2 = "abcdef".getBytes(StandardCharsets.UTF_8);
    private static final Random random = new Random();

    @Test
    public void testNonce() {
        byte[] nonce = new byte[12];
        System.out.println(bytesToHex(nonce));
        incrementNonce(nonce);
        incrementNonce(nonce);
        incrementNonce(nonce);
        incrementNonce(nonce);

        for (int i=0;i<256;i++) {
            incrementNonce(nonce);
            System.out.println(bytesToHex(nonce));
        }
    }

    @Test
    public void testChacha20Poly1305() {
        SSAEADCipher cipher = new ChaCha20Poly1305Cipher("123456");
        testSingleChunk(cipher);
        testEncryptAll(cipher);

        SSAEADCipher cipher2 = new ChaCha20Poly1305Cipher("123456");
        testMultiChunk(cipher2);

        SSAEADCipher cipher3 = new ChaCha20Poly1305Cipher("123456");
        testPartOfChunk(cipher3);
    }

    @Test
    public void testAESGCM() {
        SSAEADCipher cipher = new AESGCMCipher("123456","aes-128-gcm");
        testSingleChunk(cipher);
        testEncryptAll(cipher);

        SSAEADCipher cipher2 = new AESGCMCipher("123456","aes-256-gcm");
        testMultiChunk(cipher2);
        testEncryptAll(cipher2);

        SSAEADCipher cipher3 = new AESGCMCipher("123456","aes-256-gcm");
        testPartOfChunk(cipher3);
    }

    private void testSingleChunk(SSAEADCipher cipher) {
        byte[] ciphertext = cipher.encrypt(SOURCE);
        System.out.println(Arrays.toString(ciphertext));
        byte[] plaintext = cipher.decrypt(ciphertext);
        System.out.println(Arrays.toString(plaintext));
        System.out.println(Arrays.toString(SOURCE));
        Assert.assertArrayEquals(SOURCE,plaintext);
    }

    private void testMultiChunk(SSAEADCipher cipher) {
        byte[] ct1 = cipher.encrypt(SOURCE);
        byte[] ct2 = cipher.encrypt(SOURCE2);

        byte[] bigChunk = ArrayUtils.merge(ct1,ct2);
        byte[] pt1 = cipher.decrypt(bigChunk);
        Assert.assertArrayEquals(ArrayUtils.merge(SOURCE,SOURCE2),pt1);
    }

    private void testPartOfChunk(SSAEADCipher cipher) {
        byte[] ct1 = cipher.encrypt(SOURCE);
        byte[] ct2 = cipher.encrypt(SOURCE2);
        byte[] bigChunk = ArrayUtils.merge(ct1,ct2);

        int r = random.nextInt(ct2.length);
        System.out.println("random index: " + r);

        int index = ct1.length + r;
        byte[] p1 = new byte[index];
        System.arraycopy(bigChunk,0,p1,0,index);
        byte[] p2 = new byte[bigChunk.length - index];
        System.arraycopy(bigChunk,index,p2,0,p2.length);

        byte[] plaintext1 = cipher.decrypt(p1);
        Assert.assertArrayEquals(SOURCE,plaintext1);
        byte[] plaintext2 = cipher.decrypt(p2);
        Assert.assertArrayEquals(SOURCE2,plaintext2);
    }

    private void testEncryptAll(SSAEADCipher cipher) {
        byte[] ciphertext = cipher.encryptAll(SOURCE);
        byte[] plain = cipher.decryptAll(ciphertext);

        Assert.assertArrayEquals(SOURCE,plain);
    }

    private void incrementNonce(byte[] nonce) {
        for (int i = 0; i < nonce.length; i++) {
            nonce[i] += 1;
            //carry
            if (nonce[i] != 0) {
                break;
            }
        }
    }

    private String bytesToHex(byte[] in) {
        final StringBuilder builder = new StringBuilder();
        for(byte b : in) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }
}
