package org.wiyi.ss.core.cipher;

import org.wiyi.ss.core.cipher.impl.AESGCMCipher;
import org.wiyi.ss.core.cipher.impl.AESStreamCipher;
import org.wiyi.ss.core.cipher.impl.ChaCha20Poly1305Cipher;
import org.wiyi.ss.core.cipher.impl.Rc4Md5Cipher;
import org.wiyi.ss.core.exception.SSCipherException;

public class SSCipherFactories {
    public static SSCipher newInstance(String masterKey,String methodName) {
        if (methodName == null || methodName.equals("")) {
            throw new SSCipherException("invalid method name: { " + methodName + " }");
        }

        if (methodName.matches("aes-[0-9]{3}-gcm")) {
            return new AESGCMCipher(masterKey,methodName);
        }

        if (methodName.equalsIgnoreCase("chacha20-ietf-poly1305")) {
            return new ChaCha20Poly1305Cipher(masterKey);
        }

        if (methodName.equalsIgnoreCase("rc4-md5")) {
            return new Rc4Md5Cipher(masterKey);
        }

        if (methodName.matches("aes-[0-9]{3}-cfb")
                || methodName.matches("aes-[0-9]{3}-ctr")) {
            return new AESStreamCipher(masterKey,methodName);
        }

        throw new SSCipherException("cannot find cipher for " + methodName);
    }
}
