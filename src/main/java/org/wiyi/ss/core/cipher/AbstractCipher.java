package org.wiyi.ss.core.cipher;

import org.wiyi.ss.core.cipher.kdf.SSKDFService;

public abstract class AbstractCipher implements SSCipher {
    private byte[] sessionKey;
    private String methodName;

    public AbstractCipher(String masterKey,String methodName) {
        init(masterKey,methodName);
    }

    public void init(String masterKey,String methodName) {
        this.methodName = methodName;
        beforeInitializeKey(methodName);
        this.sessionKey = SSKDFService.KDF_SERVICE.evpBytesToKey(masterKey,keySize());
    }

    protected void beforeInitializeKey(String methodName) {

    }

    public String getMethodName() {
        return methodName;
    }

    public byte[] getSessionKey() {
        return sessionKey;
    }
}

