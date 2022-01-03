package org.wiyi.ss.net;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.socksx.v5.*;
import org.wiyi.ss.core.SSRequest;
import org.wiyi.ss.net.AttrKeys;
import org.wiyi.ss.utils.SSRequestUtils;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Objects;

public class Socks5Handler extends SimpleChannelInboundHandler<Socks5Message> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Socks5Message msg) throws Exception {
        ChannelPipeline p = ctx.pipeline();

        if (msg instanceof Socks5InitialRequest) {
            p.addBefore(ctx.name(),"socks5-command",new Socks5CommandRequestDecoder());
            ctx.writeAndFlush(new DefaultSocks5InitialResponse(Socks5AuthMethod.NO_AUTH));
        } else if (msg instanceof Socks5CommandRequest) {
            processRequest(ctx, (Socks5CommandRequest) msg);
        }
    }

    private void processRequest(ChannelHandlerContext ctx, Socks5CommandRequest request) {
        if (request.type() == Socks5CommandType.CONNECT) {
            processTCPConnect(ctx,request);
        } else if (request.type() == Socks5CommandType.UDP_ASSOCIATE) {
            processUDPAssociate(ctx,request);
        } else {
            Socks5CommandResponse res =
                    new DefaultSocks5CommandResponse(Socks5CommandStatus.FORBIDDEN,Socks5AddressType.IPv4);
            ctx.channel().writeAndFlush(res);
            ctx.close();
        }
    }

    private void processTCPConnect(ChannelHandlerContext context, Socks5CommandRequest request) {
        ChannelPipeline pipe = context.pipeline();
        Channel channel = context.channel();

        SSRequest req = SSRequestUtils.getFromSocksRequest(request);
        channel.attr(AttrKeys.ADDRESSING_FLIGHT).set(req);
        InetSocketAddress addr = (InetSocketAddress) channel.localAddress();

        Socks5CommandResponse res =
                new DefaultSocks5CommandResponse(
                        Socks5CommandStatus.SUCCESS,Socks5AddressType.IPv4,
                        addr.getHostName(),addr.getPort()
                );
        context.channel().writeAndFlush(res);
        pipe.remove(this);
        pipe.remove("socks5-command");
    }

    private void processUDPAssociate(ChannelHandlerContext context,Socks5CommandRequest request) {
        Channel channel = context.channel();
        SSRequest req = SSRequestUtils.getFromSocksRequest(request);
        channel.attr(AttrKeys.ADDRESSING_FLIGHT).set(req);

        Socks5CommandResponse res =
                new DefaultSocks5CommandResponse(
                        Socks5CommandStatus.SUCCESS,Socks5AddressType.IPv4
                );
        channel.writeAndFlush(res);

        channel.pipeline().remove(this);
        channel.pipeline().remove("socks5-command");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }
}
