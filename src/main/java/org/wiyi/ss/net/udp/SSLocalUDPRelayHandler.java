package org.wiyi.ss.net.udp;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.Attribute;
import org.wiyi.ss.core.SSConfig;
import org.wiyi.ss.core.SSRequest;
import org.wiyi.ss.core.cipher.SSCipher;
import org.wiyi.ss.core.cipher.SSCipherFactories;
import org.wiyi.ss.core.serializer.SSRequestSerializer;
import org.wiyi.ss.net.AttrKeys;
import org.wiyi.ss.net.codec.SSAddressingCodec;
import org.wiyi.ss.net.codec.SSCipherCodec;

import java.net.InetSocketAddress;

public class SSLocalUDPRelayHandler extends UDPRelayHandler{
    public static final byte[] SOCKS5_UDP_PREFIX = new byte[]{0,0,0};
    private final SSConfig config;

    public SSLocalUDPRelayHandler(SSConfig config) {
        this.config = config;
    }

    private InetSocketAddress initAddress() {
        int port = config.getUdpPort() > 0 ? config.getUdpPort() : config.getServerPort();
        return new InetSocketAddress(config.getServer(),port);
    }

    @Override
    protected InetSocketAddress initTargetAddress(ChannelHandlerContext context) {
        return initAddress();
    }

    /**
     * data received from ss-server
     */
    @Override
    protected ChannelInitializer<Channel> getRelayInitializer() {
        return new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel ch) throws Exception {
                Attribute<SSRequest> req = getSource().attr(AttrKeys.ADDRESSING_FLIGHT);
                ch.attr(AttrKeys.ADDRESSING_FLIGHT).set(req.get());

                SSCipher cipher = SSCipherFactories.newInstance(config.getPassword(),config.getMethod());
                ChannelPipeline pipe = ch.pipeline();

                pipe.addLast(new SimpleChannelInboundHandler<DatagramPacket>() {
                    @Override
                    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
                        ctx.fireChannelRead(msg.content().retain());
                    }
                });
                pipe.addLast(new SSCipherCodec(cipher,false));
                pipe.addLast(new SSAddressingCodec());
                pipe.addLast(new SimpleChannelInboundHandler<ByteBuf>() {
                    @Override
                    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
                        SSRequest req = ctx.channel().attr(AttrKeys.ADDRESS_LAND).get();
                        getSource().attr(AttrKeys.ADDRESS_LAND).set(req);

                        byte[] header = SSRequestSerializer.INSTANCE.serialize(req);

                        ByteBuf buffer = Unpooled.buffer(msg.readableBytes() + header.length);
                        buffer.writeBytes(SOCKS5_UDP_PREFIX);
                        buffer.writeBytes(header);
                        buffer.writeBytes(msg);

                        getSource().writeAndFlush(buffer);
                    }
                });
            }
        };
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }
}
