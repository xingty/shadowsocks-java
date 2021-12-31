package org.wiyi.ss.core.cipher.impl;

import org.bouncycastle.crypto.modes.AEADCipher;
import org.bouncycastle.crypto.modes.ChaCha20Poly1305;

public class ChaCha20Poly1305Cipher extends AbstractBCAEADCipher{
    public ChaCha20Poly1305Cipher(String masterKey) {
        super(masterKey,"chacha20-ietf-poly1305");
    }

    @Override
    public byte saltSize() {
        return 32;
    }

    @Override
    public int keySize() {
        return 32;
    }

    @Override
    protected AEADCipher cipher() {
        return new ChaCha20Poly1305();
    }
}
