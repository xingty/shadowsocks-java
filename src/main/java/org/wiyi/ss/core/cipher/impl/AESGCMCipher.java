package org.wiyi.ss.core.cipher.impl;

import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.modes.AEADCipher;
import org.bouncycastle.crypto.modes.GCMBlockCipher;
import org.wiyi.ss.core.cipher.AESAlgorithm;

public class AESGCMCipher extends AbstractBCAEADCipher{
    private AESAlgorithm algorithm;

    public AESGCMCipher(String masterKey,String methodName) {
        super(masterKey, methodName);
    }

    @Override
    protected void beforeInitializeKey(String methodName) {
        algorithm = AESAlgorithm.parse(methodName);
        super.beforeInitializeKey(methodName);
    }

    @Override
    public byte saltSize() {
        return (byte) (algorithm.getKeyLength() / 8);
    }

    @Override
    public int keySize() {
        return algorithm.getKeyLength() / 8;
    }

    @Override
    protected AEADCipher cipher() {
        return new GCMBlockCipher(new AESEngine());
    }
}
