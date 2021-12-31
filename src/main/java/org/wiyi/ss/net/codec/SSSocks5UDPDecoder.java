package org.wiyi.ss.net.codec;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;

import java.util.List;

public class SSSocks5UDPDecoder extends SSUDPSessionCodec{
    private static final int UDP_PREFIX = 3;

    @Override
    protected void decode(ChannelHandlerContext ctx, DatagramPacket msg, List<Object> out) throws Exception {
        int readableBytes = msg.content().readableBytes();
        if (readableBytes <= UDP_PREFIX) {
            return;
        }

        //Socks5 UDP Associate prefix
        msg.content().skipBytes(3);
        super.decode(ctx, msg, out);
    }
}
