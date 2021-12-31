/*
 * Shadowsocks is a fast tunnel proxy that helps you bypass firewalls
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.wiyi.ss.core.cipher;

public interface SSCipher {
    /**
     * Encrypt plaintext into ciphertext use internal key
     * A TCP stream cipher encrypted TCP stream starts with a randomly generated initialization vector.
     * Each UDP packet is encrypted/decrypted independently with a randomly generated initialization vector.
     * @return [IV][encrypted payload]
     */
    default byte[] encrypt(byte[] plaintext) {
        return encrypt(plaintext,0,plaintext.length);
    }

    byte[] encrypt(byte[] plaintext,int offset,int len);

    byte[] encryptAll(byte[] plaintext);

    byte[] encryptAll(byte[] key, byte [] iv,byte[] plaintext,int offset, int len);

    /**
     * Decrypt [IV][encrypted payload] into plaintext
     * @return decrypted payload
     */
    default byte[] decrypt(byte[] ciphertext) {
        return decrypt(ciphertext,0,ciphertext.length);
    }

    byte[] decrypt(byte[] ciphertext,int offset,int len);

    /**
     * UDP
     */
    byte[] decryptAll(byte[] ciphertext);

    byte[] decryptAll(byte[]key, byte[] iv,byte[] ciphertext, int offset,int len);

    /**
     * the SecretKey length
     * @return number of bytes for the key
     */
    int keySize();

    byte randomSize();

    /**
     * reset the state of the current class
     */
    void reset();
}
