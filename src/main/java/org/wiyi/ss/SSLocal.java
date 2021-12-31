package org.wiyi.ss;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.socksx.SocksPortUnificationServerHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wiyi.ss.core.SSConfig;
import org.wiyi.ss.net.Socks5Handler;
import org.wiyi.ss.net.codec.SSSocks5UDPDecoder;
import org.wiyi.ss.net.tcp.SSLocalTCPRelayHandler;
import org.wiyi.ss.net.udp.SSLocalUDPRelayHandler;

public class SSLocal implements Server{
    private static final Logger logger = LoggerFactory.getLogger(SSLocal.class);
    private final SSConfig config;
    private boolean isRunning;

    private Server tcpServer;
    private Server udpServer;

    public SSLocal(SSConfig config) {
        this.config = config;
    }

    public synchronized void start() {
        if (isRunning) {
            throw new IllegalStateException("ss-local is running.");
        }

        tcpServer = new SSLocalTCPServer();
        tcpServer.open();
        udpServer = new SSLocalUDPServer();
        udpServer.open();

        isRunning = true;
    }

    public synchronized void stop() {
        if (!isRunning) {
            throw new IllegalStateException("ss-local has been stopped.");
        }

        if (tcpServer != null && !tcpServer.isClosed()) {
            tcpServer.close();
        }

        if (udpServer != null && udpServer.isClosed()) {
            udpServer.close();
        }

        isRunning = false;
    }

    @Override
    public void open() {
        start();
    }

    @Override
    public void close() {
        stop();
    }

    @Override
    public boolean isClosed() {
        return isRunning;
    }

    private class SSLocalTCPServer implements Server{
        private EventLoopGroup acceptor = new NioEventLoopGroup();
        private EventLoopGroup worker = new NioEventLoopGroup();

        @Override
        public void open() {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(acceptor,worker).channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_RCVBUF,32 * 1024)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.SO_KEEPALIVE,true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new SocksPortUnificationServerHandler());
                            pipeline.addLast(new Socks5Handler());
                            pipeline.addLast(new SSLocalTCPRelayHandler(config));
                        }

                        @Override
                        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                            super.exceptionCaught(ctx, cause);
                            logger.error("Failed to initialize connection -> {}, cause: {}",
                                    ctx.channel().localAddress(),cause.getMessage());
                        }
                    });
            try {
                logger.info("ss-local listen on tcp {}:{}",config.getLocalAddress(),config.getLocalPort());
                bootstrap.bind(config.getLocalAddress(),config.getLocalPort()).sync();
            } catch (InterruptedException e) {
                logger.error(e.getMessage(),e);
                close();
            }
        }

        @Override
        public void close() {
            if (!acceptor.isTerminated()) {
                acceptor.shutdownGracefully();
            }
            acceptor = null;

            if (!worker.isTerminated()) {
                worker.shutdownGracefully();
            }
            worker = null;
        }

        @Override
        public boolean isClosed() {
            return acceptor.isTerminated() && worker.isTerminated();
        }
    }

    private class SSLocalUDPServer implements Server{
        private final EventLoopGroup acceptor = new NioEventLoopGroup();

        @Override
        public void open() {
            Bootstrap boot = new Bootstrap();
            boot.group(acceptor).channel(NioDatagramChannel.class)
                    .option(ChannelOption.SO_BROADCAST, false)
                    .option(ChannelOption.SO_RCVBUF, 32 * 1024)
                    .option(ChannelOption.SO_SNDBUF, 32 * 1024)
                    .handler(new ChannelInitializer<NioDatagramChannel>() {
                        @Override
                        protected void initChannel(NioDatagramChannel ctx) throws Exception {
                            ctx.pipeline().addLast(new SSSocks5UDPDecoder());
                            ctx.pipeline().addLast(new SSLocalUDPRelayHandler(config));
                        }
                    });
            try {
                logger.info("ss-local listen on udp {}:{}",config.getLocalAddress(),config.getLocalPort());
                boot.bind(config.getLocalAddress(), config.getLocalPort()).sync();
            } catch (InterruptedException e) {
                logger.error("Failed to open udp server, cause: {}", e.getMessage());
                close();
            }
        }

        @Override
        public void close() {
            if (!acceptor.isTerminated()) {
                acceptor.shutdownGracefully();
            }
        }

        @Override
        public boolean isClosed() {
            return acceptor.isTerminated();
        }
    }
}
