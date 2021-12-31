package org.wiyi.ss.core.cipher;

import org.wiyi.ss.utils.CipherUtils;

public abstract class AbstractPrimitiveCipher extends AbstractCipher implements PrimitiveCipher{


    public AbstractPrimitiveCipher(String masterKey, String methodName) {
        super(masterKey, methodName);
    }

    @Override
    public byte[] encryptAll(byte[] plaintext) {
        byte[] iv = CipherUtils.randomBytes(ivLength());

        return encryptAll(getSessionKey(),iv,plaintext,0,plaintext.length);
    }

    @Override
    public byte[] decryptAll(byte[] ciphertext) {
        byte[] iv = new byte[ivLength()];
        System.arraycopy(ciphertext,0,iv,0,iv.length);

        return decryptAll(getSessionKey(),iv,ciphertext,iv.length,ciphertext.length - iv.length);
    }

    @Override
    public byte randomSize() {
        return ivLength();
    }
}
