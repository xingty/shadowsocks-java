package org.wiyi.ss.net.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wiyi.ss.core.cipher.SSCipher;
import org.wiyi.ss.utils.ArrayUtils;

import java.util.Arrays;
import java.util.List;

public class SSCipherCodec extends MessageToMessageCodec<ByteBuf,ByteBuf> {
    private static final Logger logger = LoggerFactory.getLogger(SSCipherCodec.class);

    private final SSCipher cipher;
    private final boolean isTCP;

    public SSCipherCodec(SSCipher cipher) {
        this(cipher,true);
    }

    public SSCipherCodec(SSCipher cipher,boolean isTCP) {
        this.cipher = cipher;
        this.isTCP = isTCP;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        byte[] plaintext = ByteBufUtil.getBytes(msg);
        byte[] ciphertext = isTCP ? cipher.encrypt(plaintext) : cipher.encryptAll(plaintext);
        ByteBuf data = Unpooled.wrappedBuffer(ciphertext);
        out.add(data);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        byte[] ciphertext = ByteBufUtil.getBytes(msg);
        byte[] plaintext = isTCP ? cipher.decrypt(ciphertext) : cipher.decryptAll(ciphertext);
        if (ArrayUtils.isEmpty(plaintext)) {
            //waiting next chunk
            return;
        }

        ByteBuf data = Unpooled.wrappedBuffer(plaintext);
        out.add(data);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        logger.error("cipher error: {}",cause.getMessage());
    }
}
