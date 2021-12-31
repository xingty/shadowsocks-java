package org.wiyi.ss.core.cipher.impl;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.StreamCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.wiyi.ss.core.cipher.AbstractPrimitiveCipher;
import org.wiyi.ss.core.exception.SSCipherException;
import org.wiyi.ss.utils.ArrayUtils;
import org.wiyi.ss.utils.CipherUtils;

/**
 * Bouncy castle implementation of primitive cipher
 */
public abstract class AbstractBCPrimitiveCipher extends AbstractPrimitiveCipher {

    private StreamCipher encoder;
    private StreamCipher decoder;

    private byte[] encryptInitIV;
    private byte[] decryptInitIV;

    public AbstractBCPrimitiveCipher(String masterKey,String methodName) {
        super(masterKey, methodName);
        encoder = cipher();
        decoder = cipher();
    }

    @Override
    public byte[] encrypt(byte[] plaintext, int offset, int len) {
        if (encryptInitIV == null) {
            encryptInitIV = CipherUtils.randomBytes(ivLength());
            CipherParameters encryptParams = keyParameters(getSessionKey(),encryptInitIV);
            encoder.init(true,encryptParams);

            byte[] ciphertext = encrypt(getSessionKey(),plaintext,offset ,len);
            return ArrayUtils.merge(encryptInitIV,ciphertext);
        }

        return encrypt(getSessionKey(),plaintext,offset,len);
    }

    @Override
    public byte[] encrypt(byte[] ssKey, byte[] plaintext, int offset, int len) {
        try {
            byte[] ciphertext = new byte[len];
            encoder.processBytes(plaintext,offset,len,ciphertext,0);
            return ciphertext;
        } catch (DataLengthException e) {
            throw new SSCipherException(e.getMessage(),e.getCause());
        }
    }

    @Override
    public byte[] decrypt(byte[] ciphertext, int offset, int len) {
        if (decryptInitIV == null) {
            decryptInitIV = new byte[ivLength()];
            System.arraycopy(ciphertext,offset,decryptInitIV,0,decryptInitIV.length);
            CipherParameters decryptParams = keyParameters(getSessionKey(),decryptInitIV);
            decoder.init(false,decryptParams);

            return decrypt(getSessionKey(),ciphertext,offset + decryptInitIV.length,len - decryptInitIV.length);
        }

        return decrypt(getSessionKey(),ciphertext,offset,len);
    }

    @Override
    public byte[] decrypt(byte[] key, byte[] ciphertext, int offset, int len) {
        try {
            byte[] plaintext = new byte[len];
            decoder.processBytes(ciphertext,offset,len,plaintext,0);
            return plaintext;
        } catch (Exception e) {
            throw new SSCipherException(e.getMessage(),e);
        }
    }


    @Override
    public byte[] encryptAll(byte[] ssKey, byte[] iv, byte[] plaintext, int offset, int len) {
        int ivLen = iv.length;
        byte[] ciphertext = new byte[len + ivLen];
        CipherParameters params = keyParameters(ssKey,iv);

        encoder.init(true,params);
        encoder.processBytes(plaintext,offset,len,ciphertext,ivLen);
        System.arraycopy(iv,0,ciphertext,0,ivLen);

        return ciphertext;
    }

    @Override
    public byte[] decryptAll(byte[] key, byte[] iv, byte[] ciphertext, int offset, int len) {
        byte[] plaintext = new byte[len];

        CipherParameters params = keyParameters(key,iv);
        decoder.init(false,params);
        decoder.processBytes(ciphertext,offset,len,plaintext,0);

        return plaintext;
    }

    @Override
    public void reset() {
        encryptInitIV = null;
        decryptInitIV = null;

        encoder = cipher();
        decoder = cipher();
    }

    protected CipherParameters keyParameters(byte[] key, byte[] iv) {
        KeyParameter parameter = new KeyParameter(key);
        return new ParametersWithIV(parameter,iv);
    }

    protected abstract StreamCipher cipher();
}
