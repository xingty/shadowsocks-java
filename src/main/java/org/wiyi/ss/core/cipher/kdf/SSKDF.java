package org.wiyi.ss.core.cipher.kdf;

import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.crypto.generators.HKDFBytesGenerator;
import org.bouncycastle.crypto.params.HKDFParameters;
import org.wiyi.ss.utils.ArrayUtils;
import org.wiyi.ss.utils.Hash;

import java.nio.charset.StandardCharsets;

/**
 * Shadowsocks key derivation function
 */
public class SSKDF {
    private static final byte[] SUB_KEY = "ss-subkey".getBytes();

    public static class KeyInfo {
        public byte[] key;
        public byte[] iv;

        static KeyInfo with(byte[] key, byte[] iv) {
            KeyInfo info = new KeyInfo();
            info.key = key;
            info.iv = iv;

            return info;
        }
    }

    /**
     * the IV is redundancy now. use evpBytesToKey without ivLen
     */
    public static KeyInfo evpBytesToKey(String masterKey,int keyLen,int ivLen) {
        byte[] keys = new byte[keyLen + ivLen];
        byte[] password = masterKey.getBytes(StandardCharsets.UTF_8);

        Hash h = Hash.withMD5();

        byte[] hash = h.digest(password);
        byte[] tmp = new byte[password.length + hash.length];
        System.arraycopy(hash, 0, keys, 0, hash.length);

        for (int i=hash.length;i<keys.length;i+=hash.length) {
            System.arraycopy(hash, 0, tmp, 0, hash.length);
            System.arraycopy(password, 0, tmp, hash.length, password.length);
            hash = h.digest(tmp);
            System.arraycopy(hash, 0, keys, i, hash.length);
        }

        byte[] key = new byte[keyLen];
        System.arraycopy(keys,0,key,0,keyLen);
        byte[] iv = null;
        if (ivLen > 0) {
            iv = new byte[ivLen];
            System.arraycopy(keys,keyLen,iv,0, ivLen);
        }

        return KeyInfo.with(key,iv);
    }

    /**
     * shaodowsocks key
     * @param masterKey user master key
     * @param keyLength keyLength
     * @return The key derivation is following EVP_BytesToKey(3) in OpenSSL
     */
    public static byte[] evpBytesToKey(String masterKey,int keyLength) {
        return evpBytesToKey(masterKey,keyLength,0).key;
    }

    /**
     * AEAD key generator
     * @param ssKey shadowsocks key
     * @param salt salt
     * @param keySize keySize
     * @return HKDF(ssKey,salt,keySize)
     */
    public static byte[] aeadKey(byte[] ssKey, byte[] salt,int keySize) {
        HKDFBytesGenerator hkdf = new HKDFBytesGenerator(new SHA1Digest());
        hkdf.init(new HKDFParameters(ssKey, salt, SUB_KEY));
        byte[] key = new byte[keySize];
        hkdf.generateBytes(key, 0, keySize);

        return key;
    }

    /**
     * rc4-md5-key = md5(merge(ss-key,iv))
     */
    public static byte[] rc4md5Key(byte[] ssKey, byte[] iv) {
        return Hash.withMD5().digest(ArrayUtils.merge(ssKey,iv));
    }
}
