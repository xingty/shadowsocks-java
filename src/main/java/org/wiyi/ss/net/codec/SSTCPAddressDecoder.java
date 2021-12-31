package org.wiyi.ss.net.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import org.wiyi.ss.core.SSRequest;
import org.wiyi.ss.core.SSTCPState;
import org.wiyi.ss.core.serializer.SSRequestSerializer;
import org.wiyi.ss.net.AttrKeys;

import java.util.List;

public class SSTCPAddressDecoder extends ReplayingDecoder<SSTCPState> {
    private final SSRequestSerializer serializer = SSRequestSerializer.INSTANCE;

    public SSTCPAddressDecoder() {
        state(SSTCPState.INIT);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        switch (state()) {
            case INIT: {
                int bytes = actualReadableBytes();
                byte[] data = ByteBufUtil.getBytes(in,0,bytes);
                SSRequest request = serializer.deserialize(data);
                ByteBuf byteBuf = Unpooled.wrappedBuffer(request.getData());
                ctx.channel().attr(AttrKeys.ADDRESS_LAND).set(request);
                request.setData(null);

                out.add(byteBuf);
                in.skipBytes(bytes);
                state(SSTCPState.ESTABLISH);
                break;
            }
            case ESTABLISH: {
                int readableBytes = actualReadableBytes();
                if (readableBytes > 0) {
                    out.add(in.readRetainedSlice(readableBytes));
                }
                break;
            }
        }
    }
}
