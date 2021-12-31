package org.wiyi.ss.core.cipher.impl;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.modes.AEADCipher;
import org.bouncycastle.crypto.params.AEADParameters;
import org.bouncycastle.crypto.params.KeyParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wiyi.ss.core.cipher.AbstractAEADCipher;
import org.wiyi.ss.core.cipher.kdf.SSKDF;
import org.wiyi.ss.core.exception.SSCipherException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class AbstractBCAEADCipher extends AbstractAEADCipher {
    private static final Logger logger = LoggerFactory.getLogger(AbstractBCAEADCipher.class);

    private static final byte[] EMPTY = new byte[0];

    private final AEADCipher encoder;
    private final AEADCipher decoder;

    private int payloadLen = PAYLOAD_UNKNOWN;
    private final ByteArrayOutputStream cipherBuffer = new ByteArrayOutputStream(1024);

    public AbstractBCAEADCipher(String masterKey,String methodName) {
        super(masterKey, methodName);
        encoder = cipher();
        decoder = cipher();
    }


    /**
     * An AEAD encrypted TCP stream starts with a randomly generated salt to derive the per-session subkey,
     * followed by any number of encrypted chunks. Each chunk has the following structure:
     * [encrypted payload length][length tag][encrypted payload][payload tag]
     * The salt will be placed at the top of the chunk before session is created
     * [salt][encrypted payload length][length tag][encrypted payload][payload tag]
     * @param plaintext plaintext
     * @return [encrypted payload length][length tag][encrypted payload][payload tag]
     */
    @Override
    protected byte[] encryptWithAEAD(byte[] encryptKey, byte[] plaintext, int offset, int len) {
        byte[] nonce = getEncodeNonce();
        byte[] encryptSubKey = getEncryptSubKey();
        if (len <= CHUNK_SIZE_MASK) {
            return encryptChunk(encryptSubKey,plaintext,offset,len,nonce);
        }

        try {
            ByteArrayOutputStream ciphertextBuffer = new ByteArrayOutputStream(len + 256);
            ByteBuffer buffer = ByteBuffer.wrap(plaintext);
            buffer.position(offset);

            /*
             * if the length of the payload is greater than SSAEADCipher.CHUNK_SIZE_MASK,
             * it will be split to multi chunk
             */
            int remain;
            while ((remain = buffer.remaining()) > 0) {
                int consume = Math.min(remain, CHUNK_SIZE_MASK);
                byte[] data = encryptChunk(encryptSubKey,plaintext,buffer.position(),consume,nonce);
                ciphertextBuffer.write(data);
                buffer.position(buffer.position() + consume);
            }

            return ciphertextBuffer.toByteArray();
        } catch (IOException e) {
            throw new SSCipherException(e.getMessage(),e);
        }
    }

    /**
     * @param sessionKey TCP Session key
     * @param plaintext plaintext
     * @param offset offset
     * @param len length of plaintext
     * @param nonce encoder nonce
     * @return ciphertext
     */
    private byte[] encryptChunk(byte[] sessionKey, byte[] plaintext,int offset,int len, byte[] nonce) {
        int tagSize = tagSize();
        /*
         * the length of the ciphertext is combine by following:
         * payloadLen = 2 bytes
         * saltLen = saltSize
         * tLen = tagSize * 2 (payload and length of payload)
         * len(bytes) = payloadLen + saltLen + tLen + payload.length
         */
        byte[] ciphertext = new byte[tagSize * 2 + len + CHUNK_SIZE_LEN];
        ByteBuffer buf = ByteBuffer.wrap(ciphertext);
        buf.putShort((short) len); //current position: 2

        try {
            //encrypt payload length
            int cPos = buf.position() - 2;
            encryptChunkSize(sessionKey,nonce,ciphertext,cPos,CHUNK_SIZE_LEN,ciphertext,cPos);
            buf.position(buf.position() + tagSize);
            incrementNonce(nonce);

            encryptChunkPayload(sessionKey,nonce,plaintext,offset,len,ciphertext,buf.position());
            incrementNonce(nonce);

            return buf.array();
        } catch (Exception e) {
            throw new SSCipherException(e.getMessage(),e);
        }
    }

    /**
     * An AEAD encrypted UDP packet has the following structure
     *     [salt][encrypted payload][tag]
     * The salt is used to derive the per-session subkey and must be generated randomly to ensure uniqueness.
     * Each UDP packet is encrypted/decrypted independently, using the derived subkey and a nonce with all zero bytes.
     */
    @Override
    public byte[] encryptAll(byte[] ssKey, byte[] salt, byte[] plaintext, int offset, int len) {
        byte[] otk = SSKDF.aeadKey(ssKey,salt,keySize());
        byte[] nonce = getZeroNonce();

        return encryptMessageInternal(otk,nonce,salt,plaintext,offset,len);
    }

    private byte[] encryptMessageInternal(byte[] sessionKey, byte[] nonce,byte[] salt, byte[] plaintext, int offset, int len) {
        try {
            byte[] ciphertext = new byte[salt.length + len + tagSize()];
            ByteBuffer buf = ByteBuffer.wrap(ciphertext);
            buf.put(salt);

            encryptChunkPayload(sessionKey,nonce,plaintext,offset,len,ciphertext,buf.position());
            buf.rewind();
            return buf.array();
        } catch (InvalidCipherTextException e) {
            throw new SSCipherException(e.getMessage(),e.getCause());
        }
    }

    private void encryptChunkSize(byte[] key, byte[] nonce, byte[] in, int inOff, int inLen, byte[] out, int outOff)
            throws InvalidCipherTextException {
        encryptInternal(key,nonce,in,inOff,inLen,out,outOff);
    }

    private void encryptChunkPayload(byte[] key, byte[] nonce, byte[] in,int inOff,int inLen,byte[] out, int outOff)
            throws InvalidCipherTextException {
        encryptInternal(key,nonce,in,inOff,inLen,out,outOff);
    }

    private void encryptInternal(byte[] key, byte[] nonce, byte[] in, int inOff, int inLen, byte[] out, int outOff)
            throws InvalidCipherTextException {
        CipherParameters parameters = cipherParameters(key,nonce);
        encoder.init(true,parameters);
        int bytes = encoder.processBytes(in,inOff,inLen,out,outOff);
        encoder.doFinal(out,bytes + outOff);
    }

    @Override
    protected byte[] decryptWithAEAD(byte[] decryptSubKey, byte[] ciphertext, int offset, int len) {
        byte[] nonce = getDecodeNonce();
        return decryptChunk(decryptSubKey,nonce,ciphertext,offset,len);
    }

    private byte[] decryptChunk(byte[] sessionKey,byte[] nonce,byte[] ciphertext,int offset,int len) {
        int tagSize = tagSize();
        try {
            ByteArrayOutputStream plaintextBuffer = new ByteArrayOutputStream();
            ByteBuffer buf = ByteBuffer.wrap(ciphertext,offset,len);

            int remain;
            while ((remain = buf.remaining()) > 0) {
                int pos = buf.position();

                try {
                    /*
                     * decrypt payload
                     */
                    if (payloadLen > 0) {
                        int desire = payloadLen + tagSize - cipherBuffer.size();
                        if (desire > remain) {
                            cipherBuffer.write(ciphertext,pos,remain);
                            buf.position(buf.capacity());
                            return plaintextBuffer.size() > 0 ? plaintextBuffer.toByteArray() : EMPTY;
                        }

                        cipherBuffer.write(ciphertext,pos,desire);
                        byte[] chunkText = cipherBuffer.toByteArray();
                        byte[] pt = decryptChunkPayload(chunkText,0,chunkText.length,sessionKey,nonce,tagSize);
                        incrementNonce(nonce);

                        plaintextBuffer.write(pt);
                        buf.position(pos + desire);
                        cipherBuffer.reset();
                        payloadLen = 0;
                        continue;
                    }
                } catch (InvalidCipherTextException e) {
                    logger.error("invalid payload. error: {}", e.getMessage());
                    return plaintextBuffer.size() > 0 ? plaintextBuffer.toByteArray() : EMPTY;
                }

                try {
                    /*
                     * decrypt length of payload
                     */
                    int desire = CHUNK_SIZE_LEN + tagSize - cipherBuffer.size();
                    if (remain < desire) {
                        cipherBuffer.write(ciphertext,pos,remain);
                        buf.position(buf.capacity());
                        return plaintextBuffer.size() > 0 ? plaintextBuffer.toByteArray() : EMPTY;
                    } else {
                        cipherBuffer.write(ciphertext,pos,desire);
                        byte[] lenText = cipherBuffer.toByteArray();
                        cipherBuffer.reset();
                        buf.position(pos + desire);
                        int pLen = decryptChunkSize(lenText,0,lenText.length,sessionKey,nonce);
                        incrementNonce(nonce);

                        //maybe pretty
                        if (pLen <= 0) { break; }
                        this.payloadLen = pLen;
                    }
                } catch (InvalidCipherTextException e) {
                    logger.error("invalid payload tag, cause: {}", e.getMessage());
                    return plaintextBuffer.size() > 0 ? plaintextBuffer.toByteArray() : EMPTY;
                }
            }

            return plaintextBuffer.toByteArray();
        } catch (IOException e) {
            return EMPTY;
        }
    }

    private byte[] decryptChunkPayload(byte[] ciphertext,int offset,int len,byte[] key,byte[] nonce,int tagSize)
            throws InvalidCipherTextException {
        byte[] plaintext = new byte[len - tagSize];
        CipherParameters p2 = cipherParameters(key,nonce);
        decoder.init(false,p2);
        int bytes = decoder.processBytes(ciphertext,offset,len,plaintext,0);
        decoder.doFinal(plaintext,bytes);

        return plaintext;
    }

    private int decryptChunkSize(byte[] ciphertext,int offset,int len,byte[] key,byte[] nonce) throws InvalidCipherTextException {
        byte[] p = new byte[2];
        CipherParameters p1 = cipherParameters(key,nonce);
        decoder.init(false,p1);
        int bytes = decoder.processBytes(ciphertext,offset,len,p,0);
        decoder.doFinal(p,bytes);

        //convert bytes to unsigned short
        return ((p[0] << 8) | (p[1] & 0xFF));
    }

    /**
     * An AEAD encrypted UDP packet has the following structure
     *     [salt][encrypted payload][tag]
     * The salt is used to derive the per-session subkey and must be generated randomly to ensure uniqueness.
     * Each UDP packet is encrypted/decrypted independently, using the derived subkey and a nonce with all zero bytes.
     * @param ssKey shadowsocks key
     * @param salt salt
     * @param ciphertext ciphertext
     * @return plaintext
     */
    @Override
    public byte[] decryptAll(byte[] ssKey, byte[] salt, byte[] ciphertext, int offset, int len) {
        byte[] otk = SSKDF.aeadKey(ssKey,salt,keySize());
        byte[] nonce = getZeroNonce();

        return decryptInternal(otk,nonce,ciphertext,offset,len);
    }

    /**
     *
     * @param otk one-time key
     * @param nonce nonce
     * @param ciphertext ciphertext
     */
    private byte[] decryptInternal(byte[] otk,byte[] nonce,byte[] ciphertext, int offset, int len) {
        try {
            return decryptChunkPayload(ciphertext,offset,len,otk,nonce,tagSize());
        } catch (InvalidCipherTextException e) {
            throw new SSCipherException(e.getMessage(),e.getCause());
        }
    }

    protected CipherParameters cipherParameters(byte[] key,byte[] nonce) {
        int macSize = tagSize() * 8;
        KeyParameter kp = new KeyParameter(key);
        return new AEADParameters(kp,macSize,nonce);
    }

    protected abstract AEADCipher cipher();
}
