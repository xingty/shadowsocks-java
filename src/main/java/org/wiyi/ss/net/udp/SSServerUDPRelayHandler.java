package org.wiyi.ss.net.udp;

import io.netty.channel.*;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wiyi.ss.core.SSRequest;
import org.wiyi.ss.net.AttrKeys;
import org.wiyi.ss.utils.SSRequestUtils;

import java.net.InetSocketAddress;

public class SSServerUDPRelayHandler extends UDPRelayHandler{
    private static final Logger logger = LoggerFactory.getLogger(SSServerUDPRelayHandler.class);
    private InetSocketAddress address;

    private void initAddress(ChannelHandlerContext context) {
        SSRequest request = context.channel().attr(AttrKeys.ADDRESS_LAND).get();
        if (request != null) {
            this.address = new InetSocketAddress(request.getHost(),request.getPort());
        }
    }

    @Override
    protected InetSocketAddress initTargetAddress(ChannelHandlerContext context) {
        if (address == null) {
            initAddress(context);
        }

        return address;
    }

    @Override
    protected ChannelInitializer<Channel> getRelayInitializer() {
        return new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel ch) throws Exception {
                ChannelPipeline pipe = ch.pipeline();
                pipe.addLast(new IdleStateHandler(0,0,30){
                    @Override
                    protected void channelIdle(ChannelHandlerContext ctx, IdleStateEvent evt) throws Exception {
                        super.channelIdle(ctx, evt);
                        closeConnection();
                    }
                });
                pipe.addLast(new SimpleChannelInboundHandler<DatagramPacket>() {
                    @Override
                    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
                        SSRequest req = SSRequestUtils.getFromInetAddress(msg.sender());
                        getSource().attr(AttrKeys.ADDRESSING_FLIGHT).set(req);
                        getSource().writeAndFlush(msg.content());
                    }
                });
            }
        };
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        // Terminate exception passing to avoid connection closure by downstream handlers
        Channel channel = ctx.channel();
        logger.error("udp tunnel {} -> {}, fail: {}",
                channel.localAddress(),channel.remoteAddress(),cause.getMessage());
    }
}
