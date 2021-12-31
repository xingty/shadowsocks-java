package org.wiyi.ss.udp;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class UDPServer {

    public static void main(String[] args) {
        NioEventLoopGroup acceptor = new NioEventLoopGroup();
        Bootstrap boot = new Bootstrap();
        boot.group(acceptor).channel(NioDatagramChannel.class)
                .option(ChannelOption.SO_BROADCAST, false)
                .handler(new ChannelInitializer<NioDatagramChannel>() {
                    @Override
                    protected void initChannel(NioDatagramChannel ctx) throws Exception {
                        ChannelPipeline pipe = ctx.pipeline();
                        pipe.addLast(new SimpleChannelInboundHandler<DatagramPacket>() {
                            @Override
                            protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
                                System.out.println(Arrays.toString(ByteBufUtil.getBytes(msg.content())));
                                ByteBuf buf = Unpooled.wrappedBuffer("hello world!\n".getBytes(StandardCharsets.UTF_8));
                                ctx.channel().writeAndFlush(new DatagramPacket(buf, msg.sender()));
                            }
                        });
                    }
                });
        try {
            boot.bind("127.0.0.1", 1086).sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
