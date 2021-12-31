package org.wiyi.ss.net.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageCodec;
import org.wiyi.ss.net.AttrKeys;

import java.net.InetSocketAddress;
import java.util.List;

public class SSUDPSessionCodec extends MessageToMessageCodec<DatagramPacket, ByteBuf> {
    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        InetSocketAddress address = ctx.channel().attr(AttrKeys.UDP_SENDER).get();
        ctx.channel().writeAndFlush(new DatagramPacket(msg.retain(),address));
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, DatagramPacket msg, List<Object> out) throws Exception {
        ctx.channel().attr(AttrKeys.UDP_SENDER).set(msg.sender());
        ctx.fireChannelRead(msg.content().retain());
    }
}
