package org.wiyi.ss.core.cipher;

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
public interface SSAEADCipher extends SSCipher {
    int PAYLOAD_UNKNOWN = 0;
    int CHUNK_SIZE_LEN = 2;
    int CHUNK_SIZE_MASK = 0x3FFF;


    int DEFAULT_NONCE_SIZE = 12;
    int DEFAULT_TAG_SIZE = 16;

    /**
     * macSize in bits
     */
    default byte tagSize() {
        return DEFAULT_TAG_SIZE;
    }

    default byte nonceSize() {
        return DEFAULT_NONCE_SIZE;
    }

    /**
     * length of salt
     * The salt is used to derive the per-session subkey and must be generated randomly to ensure uniqueness
     * @return number of bytes of the salt
     */
    byte saltSize();

    @Override
    default byte randomSize() {
        return nonceSize();
    }
}
