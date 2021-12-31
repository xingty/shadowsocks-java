package org.wiyi.ss.cipher;

import org.junit.Assert;
import org.junit.Test;
import org.wiyi.ss.core.cipher.kdf.SSKDF;

import java.util.Arrays;


public class KDFTest {

    @Test
    public void testKDF() {
        String password = "Qi7IDbmERAAK0ckLlmJYOA==";
        System.out.println(password.length());

        byte[] p1 = SSKDF.evpBytesToKey(password,16);
        System.out.println(Arrays.toString(p1));
        SSKDF.KeyInfo info = SSKDF.evpBytesToKey(password,16,0);

        Assert.assertArrayEquals(p1,info.key);

        byte[] p3 = SSKDF.evpBytesToKey(password,32);
        System.out.println(Arrays.toString(p3));
        SSKDF.KeyInfo info2 = SSKDF.evpBytesToKey(password,32,0);
        Assert.assertArrayEquals(p3,info2.key);

        SSKDF.KeyInfo keyInfo = SSKDF.evpBytesToKey(password,16,16);
        Assert.assertEquals(16,keyInfo.iv.length);
    }
}
