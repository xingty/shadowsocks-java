package org.wiyi.ss.core.cipher;

import org.wiyi.ss.core.cipher.kdf.SSKDF;
import org.wiyi.ss.utils.ArrayUtils;
import org.wiyi.ss.utils.CipherUtils;

/**
 * AEAD stands for Authenticated Encryption with Associated Data.
 * AEAD ciphers simultaneously provide confidentiality, integrity, and authenticity.
 * They have excellent performance and power efficiency on modern hardware.
 * Users should use AEAD ciphers whenever possible.
 *
 * The following AEAD ciphers are recommended.
 * Compliant Shadowsocks implementations must support chacha20-ietf-poly1305.
 * Implementations for devices with hardware AES acceleration should also implement
 * aes-128-gcm, aes-192-gcm, and aes-256-gcm.
 *
 * <b>Key Derivation</b>
 * The master key can be input directly from user or generated from a password.
 * The key derivation is still following EVP_BytesToKey(3) in OpenSSL like stream ciphers.
 * HKDF_SHA1 is a function that takes a secret key, a non-secret salt, an info string,
 * and produces a subkey that is cryptographically strong even if the input secret key is weak.
 *     HKDF_SHA1(key, salt, info) => subkey
 * The info string binds the generated subkey to a specific application context.
 * In our case, it must be the string “ss-subkey” without quotes.
 * We derive a per-session subkey from a pre-shared master key using HKDF_SHA1.
 * Salt must be unique through the entire life of the pre-shared master key.
 *
 * <b>Authenticated Encryption/Decryption</b>
 * AE_encrypt is a function that takes a secret key, a non-secret nonce, a message,
 * and produces ciphertext and authentication tag. Nonce must be unique for a given key in each invocation.
 *     AE_encrypt(key, nonce, message) => (ciphertext, tag)
 * AE_decrypt is a function that takes a secret key, non-secret nonce, ciphertext, authentication tag,
 * and produces original message. If any of the input is tam- pered with, decryption will fail.
 *     AE_decrypt(key, nonce, ciphertext, tag) => message
 *
 * <b>TCP</b>
 * An AEAD encrypted TCP stream starts with a randomly generated salt to derive the per-session subkey,
 * followed by any number of encrypted chunks. Each chunk has the following structure:
 *     [encrypted payload length][length tag][encrypted payload][payload tag]
 * Payload length is a 2-byte big-endian unsigned integer capped at 0x3FFF.
 * The higher two bits are reserved and must be set to zero. Payload is therefore limited to 16*1024 - 1 bytes.
 * The first AEAD encrypt/decrypt operation uses a counting nonce starting from 0.
 * After each encrypt/decrypt operation, the nonce is incremented by one as if it were an unsigned little-endian integer.
 * Note that each TCP chunk involves two AEAD encrypt/decrypt operation: one for the payload length, and one for the payload.
 * Therefore each chunk increases the nonce twice.
 *
 * <b>UDP</b>
 * An AEAD encrypted UDP packet has the following structure
 *     [salt][encrypted payload][tag]
 * The salt is used to derive the per-session subkey and must be generated randomly to ensure uniqueness.
 * Each UDP packet is encrypted/decrypted independently, using the derived subkey and a nonce with all zero bytes.
 */
public abstract class AbstractAEADCipher extends AbstractCipher implements SSAEADCipher {
    protected byte[] encodeNonce;
    protected byte[] decodeNonce;
    private byte[] defaultNonce;

    private byte[] encryptSubKey;
    private byte[] decryptSubKey;

    public AbstractAEADCipher(String masterKey,String methodName) {
        super(masterKey,methodName);
        init(masterKey,methodName);
    }

    @Override
    public void init(String masterKey, String methodName) {
        super.init(masterKey, methodName);
        this.encodeNonce = new byte[nonceSize()];
        this.decodeNonce = new byte[nonceSize()];
        this.defaultNonce = new byte[nonceSize()];
    }

    @Override
    public byte[] encrypt(byte[] plaintext, int offset, int len) {
        if (encryptSubKey == null) {
            byte[] salt = CipherUtils.randomBytes(saltSize());
            encryptSubKey = SSKDF.aeadKey(getSessionKey(),salt,keySize());

            byte[] ciphertext = encryptWithAEAD(encryptSubKey,plaintext,offset,len);
            return ArrayUtils.merge(salt,ciphertext);
        }

        return encryptWithAEAD(encryptSubKey,plaintext,offset,len);
    }

    @Override
    public byte[] decrypt(byte[] ciphertext, int offset, int len) {
        if (decryptSubKey == null) {
            byte[] salt = new byte[saltSize()];
            System.arraycopy(ciphertext,0,salt,0,salt.length);
            decryptSubKey = SSKDF.aeadKey(getSessionKey(),salt,keySize());

            return decryptWithAEAD(decryptSubKey,ciphertext,salt.length + offset,len - salt.length);
        }

        return decryptWithAEAD(decryptSubKey,ciphertext,offset,len);
    }

    @Override
    public byte[] encryptAll(byte[] plaintext) {
        byte[] salt = CipherUtils.randomBytes(saltSize());
        return encryptAll(getSessionKey(),salt,plaintext,0,plaintext.length);
    }

    @Override
    public byte[] decryptAll(byte[] ciphertext) {
        byte[] salt = new byte[saltSize()];
        System.arraycopy(ciphertext,0,salt,0,salt.length);

        return decryptAll(getSessionKey(),salt,ciphertext,salt.length,ciphertext.length - salt.length);
    }

    @Override
    public void reset() {
        decodeNonce = null;
        encodeNonce = null;
        encryptSubKey = null;
        decryptSubKey = null;
    }

    protected abstract byte[] encryptWithAEAD(byte[] decryptSubKey, byte[] plaintext, int offset, int len);
    protected abstract byte[] decryptWithAEAD(byte[] decryptSubKey, byte[] ciphertext, int offset, int len);

    public byte[] getEncryptSubKey() {
        return encryptSubKey;
    }

    public byte[] getDecryptSubKey() {
        return decryptSubKey;
    }

    public byte[] getEncodeNonce() {
        return encodeNonce;
    }

    public byte[] getDecodeNonce() {
        return decodeNonce;
    }

    public byte[] getZeroNonce() {
        return defaultNonce;
    }

    /**
     * the nonce is incremented by one as if it were an unsigned little-endian integer
     */
    protected void incrementNonce(byte[] nonce) {
        for (int i = 0; i < nonce.length; i++) {
            nonce[i] += 1;
            //carry
            if (nonce[i] != 0) {
                break;
            }
        }
    }

    @Override
    public byte randomSize() {
        return saltSize();
    }
}
