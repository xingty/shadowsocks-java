package org.wiyi.ss.net.tcp;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public abstract class TCPRelayHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private static final int TIMEOUT = 30 * 1000;
    private static final Logger logger = LoggerFactory.getLogger(TCPRelayHandler.class);

    /**
     * Cache streams that arrive before the target channel is established
     */
    private final List<ByteBuf> buffer = new ArrayList<>(10);
    private Channel source;
    private Channel target;

    @Override
    protected void channelRead0(ChannelHandlerContext context, ByteBuf data) throws Exception {
        if (data == null || data.readableBytes() <= 0) {
            return;
        }

        if (source != null && target != null) {
            doRelay(target,data.retain());
        }
        else {
            this.source = context.channel();
            buffer.add(data.retain());
            initRelayChannel(context);
        }
    }

    private void initRelayChannel(ChannelHandlerContext context) {
        InetSocketAddress address = getRelayAddress(context);
        if (address == null) {
            logger.error("{} socks5 request is null, localAddr: {}",getName(),context.channel().localAddress());
            return;
        }

        createTargetChannel(context,address);
    }

    /**
     * create a channel to connect to target host
     */
    private void createTargetChannel(ChannelHandlerContext context, InetSocketAddress address) {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(context.channel().eventLoop())
                .channel(NioSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, TIMEOUT)
                .option(ChannelOption.SO_KEEPALIVE,true)
                .option(ChannelOption.SO_RCVBUF, 16 * 1024)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(getRelayInitializer())
                .connect(address)
                .addListener((ChannelFutureListener) future -> {
                    if (!future.isSuccess()) {
                        onTargetChannelConnectFailure(future);
                        return;
                    }

                    target = future.channel();
                    doRelay(target,null);
                });
    }

    private void doRelay(Channel target,ByteBuf data) {
        if (!buffer.isEmpty()) {
            buffer.forEach(target::writeAndFlush);
            buffer.clear();
        }

        if (data != null) {
            target.writeAndFlush(data);
        }
    }

    protected abstract ChannelInitializer<SocketChannel> getRelayInitializer();
    protected abstract InetSocketAddress getRelayAddress(ChannelHandlerContext context);
    protected abstract void onTargetChannelConnectFailure(ChannelFuture future);

    public Channel getSource() {
        return source;
    }

    public Channel getTarget() {
        return target;
    }

    protected void closeConnection() {
        try {
            if (target != null && target.isOpen()) {
                target.close();
            }

            if (source != null && source.isOpen()) {
                source.close();
            }

            if (!buffer.isEmpty()) {
                buffer.forEach(ReferenceCountUtil::safeRelease);
                buffer.clear();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        }
    }

    protected String getName() {
        return "tcp-relay";
    }
}
