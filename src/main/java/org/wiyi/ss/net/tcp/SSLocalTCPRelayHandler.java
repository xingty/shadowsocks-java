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
import org.wiyi.ss.core.cipher.SSCipher;
import org.wiyi.ss.core.cipher.SSCipherFactories;
import org.wiyi.ss.net.AttrKeys;
import org.wiyi.ss.net.codec.SSCipherCodec;
import org.wiyi.ss.net.codec.SSTCPAddressEncoder;


import java.net.InetSocketAddress;

public class SSLocalTCPRelayHandler extends TCPRelayHandler{
    private static final Logger logger = LoggerFactory.getLogger(SSLocalTCPRelayHandler.class);

    private final SSConfig config;

    public SSLocalTCPRelayHandler(SSConfig config) {
        this.config = config;
    }

    @Override
    protected InetSocketAddress getRelayAddress(ChannelHandlerContext context) {
        return new InetSocketAddress(config.getServer(),config.getServerPort());
    }

    @Override
    protected ChannelInitializer<SocketChannel> getRelayInitializer() {
        return new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                SSRequest req = getSource().attr(AttrKeys.ADDRESSING_FLIGHT).get();
                SSCipher cipher = SSCipherFactories.newInstance(config.getPassword(),config.getMethod());

                ch.pipeline()
                        .addLast(new IdleStateHandler(0,0,config.getTimeout()){
                            @Override
                            protected IdleStateEvent newIdleStateEvent(IdleState state, boolean first) {
                                closeConnection();
                                return super.newIdleStateEvent(state, first);
                            }
                        })
                        .addLast(new SSCipherCodec(cipher))
                        .addLast(new SSTCPAddressEncoder(req))
                        .addLast(new SimpleChannelInboundHandler<ByteBuf>() {
                            @Override
                            protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
                                getSource().writeAndFlush(msg.retain());
                            }

                            @Override
                            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                                logger.error("exception on tcp relay channel, cause: {}", cause.getMessage());
                                closeConnection();
                            }
                        });
            }
        };
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("exception on tcp tunnel -> {}:{}, cause: {}"
                ,config.getServer(),config.getServerPort(),cause.getMessage());
        super.exceptionCaught(ctx, cause);
        closeConnection();
    }

    @Override
    protected void onTargetChannelConnectFailure(ChannelFuture future) {
        logger.error("Failed to connect to the server -> {}:{}, cause: {}",
                config.getServer(),config.getServerPort(),future.cause().getMessage());
        closeConnection();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        closeConnection();
    }

    @Override
    protected String getName() {
        return "ss-local-relay";
    }
}
