package org.wiyi.ss.core.cipher.kdf;

import org.wiyi.ss.core.LRUCache;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SSKDFService {
    public static final SSKDFService KDF_SERVICE = new SSKDFService();

    private final LRUCache<String,byte[]> lruCache = new LRUCache<>(10);
    private final Lock lock = new ReentrantLock();

    /**
     * The key can be input directly from user or generated from a password.
     * The key derivation is following EVP_BytesToKey(3) in OpenSSL
     */
    public byte[] evpBytesToKey(String masterKey, int keyLength) {
        String key = masterKey + "-" + keyLength;
        if (lruCache.containsKey(key)) {
            return lruCache.get(key);
        }

        return initKey(key,masterKey,keyLength);
    }

    private byte[] initKey(String key,String masterKey,int keyLength) {
        try {
            lock.lock();
            if (lruCache.containsKey(key)) {
                return lruCache.get(key);
            }

            byte[] ssKey = SSKDF.evpBytesToKey(masterKey,keyLength);
            lruCache.put(key,ssKey);

            return ssKey;
        } finally {
            lock.unlock();
        }
    }
}
