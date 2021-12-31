package org.wiyi.ss.net.tcp;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wiyi.ss.core.SSConfig;
import org.wiyi.ss.core.SSRequest;
import org.wiyi.ss.net.AttrKeys;

import java.net.InetSocketAddress;

public class SSServerTCPRelayHandler extends TCPRelayHandler{
    private static final Logger logger = LoggerFactory.getLogger(SSServerTCPRelayHandler.class);

    private final SSConfig config;

    public SSServerTCPRelayHandler(SSConfig config) {
        this.config = config;
    }


    @Override
    protected InetSocketAddress getRelayAddress(ChannelHandlerContext context) {
        SSRequest request = context.channel().attr(AttrKeys.ADDRESS_LAND).get();
        if (request != null) {
            return new InetSocketAddress(request.getHost(),request.getPort());
        }

        return null;
    }

    @Override
    protected ChannelInitializer<SocketChannel> getRelayInitializer() {
        return new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline()
                        .addLast(new IdleStateHandler(0,0,config.getTimeout()) {
                            @Override
                            protected IdleStateEvent newIdleStateEvent(IdleState state, boolean first) {
                                closeConnection();
                                return super.newIdleStateEvent(state, first);
                            }
                        })
                        .addLast(new SimpleChannelInboundHandler<ByteBuf>() {
                            @Override
                            protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
                                getSource().writeAndFlush(msg);
                            }
                        })
                ;
            }
        };
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        // Terminate exception passing to avoid connection closure by downstream handlers
        logger.error("tcp tunnel {} -> {}, error: {}",
                ctx.channel().localAddress(),ctx.channel().remoteAddress(),cause.getMessage());
    }

    @Override
    protected void onTargetChannelConnectFailure(ChannelFuture future) {
        //!!! Do not close the connection immediately to prevent active probing by GFW
    }

    @Override
    protected String getName() {
        return "ss-server-tcp-relay";
    }
}
