package org.wiyi.ss.net.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.wiyi.ss.core.SSRequest;
import org.wiyi.ss.core.serializer.SSRequestSerializer;

public class SSTCPAddressEncoder extends MessageToByteEncoder<ByteBuf> {
    private final SSRequestSerializer serializer = SSRequestSerializer.INSTANCE;
    private SSRequest request;

    public SSTCPAddressEncoder(SSRequest request) {
        this.request = request;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out) throws Exception {
        if (request == null) {
            out.writeBytes(msg.retain());
            return;
        }

        byte[] payload = ByteBufUtil.getBytes(msg);
        request.setData(payload);

        byte[] data = serializer.serialize(request);
        out.writeBytes(data);
        request = null;
    }
}
