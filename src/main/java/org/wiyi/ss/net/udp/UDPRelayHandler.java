package org.wiyi.ss.net.udp;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioDatagramChannel;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

/**
 *  SOCKS5 UDP Request
 *  +----+------+------+----------+----------+----------+
 *  |RSV | FRAG | ATYP | DST.ADDR | DST.PORT |   DATA   |
 *  +----+------+------+----------+----------+----------+
 *  | 2  |  1   |  1   | Variable |    2     | Variable |
 *  +----+------+------+----------+----------+----------+
 *
 *  SOCKS5 UDP Response
 *  +----+------+------+----------+----------+----------+
 *  |RSV | FRAG | ATYP | DST.ADDR | DST.PORT |   DATA   |
 *  +----+------+------+----------+----------+----------+
 *  | 2  |  1   |  1   | Variable |    2     | Variable |
 *  +----+------+------+----------+----------+----------+
 *
 *  shadowsocks UDP Request (before encrypted)
 *  +------+----------+----------+----------+
 *  | ATYP | DST.ADDR | DST.PORT |   DATA   |
 *  +------+----------+----------+----------+
 *  |  1   | Variable |    2     | Variable |
 *  +------+----------+----------+----------+
 *
 *  shadowsocks UDP Response (before encrypted)
 *  +------+----------+----------+----------+
 *  | ATYP | DST.ADDR | DST.PORT |   DATA   |
 *  +------+----------+----------+----------+
 *  |  1   | Variable |    2     | Variable |
 *  +------+----------+----------+----------+
 *
 *  shadowsocks UDP Request and Response (after encrypted)
 *  +-------+--------------+
 *  |   IV  |    PAYLOAD   |
 *  +-------+--------------+
 *  | Fixed |   Variable   |
 *  +-------+--------------+
 */
public abstract class UDPRelayHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private final UDPConnectionHolder holder = UDPConnectionHolder.HOLDER;
    private final List<ByteBuf> buffer = new ArrayList<>();

    private Channel source;
    private Channel target;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf data) throws Exception {
        if (data.readableBytes() <= 0) {
            return;
        }

        if (source == null && target == null) {
            buffer.add(data.retain());
            source = ctx.channel();
            InetSocketAddress targetHost = initTargetAddress(ctx);
            initUDPRelayChannel(ctx,targetHost);
        } else {
            doRelay(data.retain());
        }
    }

    private void initUDPRelayChannel(ChannelHandlerContext ctx,InetSocketAddress address) throws InterruptedException {
        Bootstrap boot = new Bootstrap();
        boot.group(ctx.channel().eventLoop())
                .channel(NioDatagramChannel.class)
                .option(ChannelOption.SO_BROADCAST, false)
                .handler(getRelayInitializer())
                .connect(address)
                .addListener((ChannelFutureListener) future -> {
                    if (!future.isSuccess()) {
                        System.err.println("channel init error");
                        return;
                    }

                    target = future.channel();
                    doRelay(null);
                });
    }

    protected abstract InetSocketAddress initTargetAddress(ChannelHandlerContext context);
    protected abstract ChannelInitializer<Channel> getRelayInitializer();

    private void doRelay(ByteBuf buf) {
        if (!buffer.isEmpty()) {
            buffer.forEach(target::writeAndFlush);
            buffer.clear();
        }

        if (buf != null) {
            target.writeAndFlush(buf);
        }
    }

    protected void closeConnection() {
        if (target != null && target.isOpen()) {
            target.close();
        }

        source = null;
        target = null;
    }

    public Channel getSource() {
        return source;
    }

    public Channel getTarget() {
        return target;
    }
}
