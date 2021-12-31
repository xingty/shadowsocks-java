package org.wiyi.ss.core.cipher.impl;

import org.bouncycastle.crypto.StreamCipher;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.modes.CFBBlockCipher;
import org.bouncycastle.crypto.modes.OFBBlockCipher;
import org.bouncycastle.crypto.modes.SICBlockCipher;
import org.wiyi.ss.core.cipher.AESAlgorithm;

public class AESStreamCipher extends AbstractBCPrimitiveCipher{
    private AESAlgorithm algorithm;

    public AESStreamCipher(String masterKey,String methodName) {
        super(masterKey, methodName);
    }

    @Override
    protected void beforeInitializeKey(String methodName) {
        this.algorithm = AESAlgorithm.parse(methodName);
    }

    @Override
    public int keySize() {
        return algorithm.getKeyLength() / 8;
    }

    @Override
    public byte ivLength() {
        return 16;
    }

    @Override
    protected StreamCipher cipher() {
        AESEngine engine = new AESEngine();
        return getByMode(engine, algorithm.getMode());
    }

    private StreamCipher getByMode(AESEngine engine,String mode) {
        int bitBlockSize = ivLength() * 8;
        switch (mode) {
            case "CFB":
                return new CFBBlockCipher(engine, bitBlockSize);
            case "OFB":
                return new OFBBlockCipher(engine, bitBlockSize);
            case "CTR":
                return new SICBlockCipher(engine);
            default:
                throw new IllegalArgumentException("un support mode: " + mode);
        }
    }
}
