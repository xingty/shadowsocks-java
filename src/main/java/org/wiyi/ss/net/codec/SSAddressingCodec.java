package org.wiyi.ss.net.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wiyi.ss.core.SSRequest;
import org.wiyi.ss.core.exception.SSSerializationException;
import org.wiyi.ss.core.serializer.SSRequestSerializer;
import org.wiyi.ss.net.AttrKeys;

import java.util.List;

/**
 * Addresses used in Shadowsocks follow the SOCKS5 address format:
 * [1-byte type][variable-length host][2-byte port]
 */
public class SSAddressingCodec extends MessageToMessageCodec<ByteBuf,ByteBuf> {
    private static final Logger logger = LoggerFactory.getLogger(SSAddressingCodec.class);
    private final SSRequestSerializer serializer = SSRequestSerializer.INSTANCE;

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        /*
         * For ss-local, the SSRequest is from the addressing in Socks5 request
         * For ss-server, the SSRequest is from the target host
         */
        SSRequest request = ctx.channel().attr(AttrKeys.ADDRESSING_FLIGHT).get();
        if (request == null) {
            logger.error("addressing flight is empty");
            return;
        }

        byte[] bytes = ByteBufUtil.getBytes(msg);
        request.setData(bytes);

        byte[] data = serializer.serialize(request);
        out.add(Unpooled.wrappedBuffer(data));
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        byte[] data = ByteBufUtil.getBytes(msg);
        SSRequest request = serializer.deserialize(data);
        if (request == null) { return; }

        byte[] payload = request.getData();
        msg.clear().writeBytes(payload).retain();
        out.add(msg);
        request.setData(null);

        ctx.channel().attr(AttrKeys.ADDRESS_LAND).set(request);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof SSSerializationException) {
            logger.error("serializer/deserializer failed. cause: {}",cause.getMessage());
            return;
        }

        logger.error("addressing error: {}",cause.getMessage());
    }
}
