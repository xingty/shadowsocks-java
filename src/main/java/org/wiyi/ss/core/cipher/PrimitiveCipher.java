/*
 * Shadowsocks is a fast tunnel proxy that helps you bypass firewalls
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.wiyi.ss.core.cipher;

/**
 * Stream_encrypt is a function that takes a secret message,
 * and produces a ciphertext with the same length as the message.
 *
 *  Stream_encrypt(key, IV, message) => ciphertext
 *
 * Stream_decrypt is a function that takes a secret key, an initialization vector, a
 * ciphertext, and produces the original message.
 *     Stream_decrypt(key, IV, ciphertext) => message
 * The key can be input directly from user or generated from a password. The key derivation is following EVP_BytesToKey(3) in OpenSSL. The detailed spec can be found here.
 *
 * TCP
 * A stream cipher encrypted TCP stream starts with a randomly generated initialization vector, followed by encrypted payload data.
 *     [IV][encrypted payload]
 * UDP
 * A stream cipher encrypted UDP packet has the following structure
 *     [IV][encrypted payload]
 * Each UDP packet is encrypted/decrypted independently with a randomly generated initialization vector.
 */
public interface PrimitiveCipher extends SSCipher {

    /**
     * encrypt data
     * @param key key
     * @param plaintext plaintext
     * @param offset the offset in {@code plaintext}
     * @param len the plaintext length
     * @return the new buffer with the result
     */
    byte[] encrypt(byte[] key, byte[] plaintext, int offset, int len);

    /**
     * decrypt data
     * @param key key
     * @param ciphertext ciphertext
     * @param offset the offset in {@code ciphertext}
     * @param len the plaintext length
     * @return the new buffer with the result
     */
    byte[] decrypt(byte[] key,byte[] ciphertext,int offset,int len);

    /**
     * length of initialization vector
     */
    byte ivLength();

    @Override
    default byte randomSize() {
        return ivLength();
    }
}
