package org.wiyi.ss.core.cipher.impl;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.StreamCipher;
import org.bouncycastle.crypto.engines.RC4Engine;
import org.bouncycastle.crypto.params.KeyParameter;
import org.wiyi.ss.core.cipher.kdf.SSKDF;

public class Rc4Md5Cipher extends AbstractBCPrimitiveCipher {

    public Rc4Md5Cipher(String masterKey) {
        super(masterKey,"rc4-md5");
    }

    @Override
    public int keySize() {
        return 16;
    }

    @Override
    public byte ivLength() {
        return 16;
    }

    @Override
    protected CipherParameters keyParameters(byte[] key, byte[] iv) {
        return new KeyParameter(SSKDF.rc4md5Key(key,iv));
    }

    @Override
    protected StreamCipher cipher() {
        return new RC4Engine();
    }
}
