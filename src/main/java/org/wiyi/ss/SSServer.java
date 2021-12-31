package org.wiyi.ss;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wiyi.ss.core.SSConfig;
import org.wiyi.ss.core.cipher.SSCipher;
import org.wiyi.ss.core.cipher.SSCipherFactories;
import org.wiyi.ss.net.codec.SSAddressingCodec;
import org.wiyi.ss.net.codec.SSCipherCodec;
import org.wiyi.ss.net.codec.SSTCPAddressDecoder;
import org.wiyi.ss.net.codec.SSUDPSessionCodec;
import org.wiyi.ss.net.tcp.SSServerTCPRelayHandler;
import org.wiyi.ss.net.udp.SSServerUDPRelayHandler;

public class SSServer implements Server{
    private static final Logger logger = LoggerFactory.getLogger(SSServer.class);

    private final SSConfig config;
    private boolean isRunning;
    private Server tcpServer;
    private Server udpServer;

    public SSServer(SSConfig config) {
        this.config = config;
    }

    public synchronized void start() {
        if (isRunning) {
            throw new IllegalStateException("ss-local is running.");
        }

        tcpServer = new SSServerTCPServer();
        tcpServer.open();
        udpServer = new SSServerUDPServer();
        udpServer.open();

        isRunning = true;
    }

    public synchronized void stop() {
        if (isRunning) {
            return;
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

    private class SSServerTCPServer implements Server{
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
                            SSCipher cipher = SSCipherFactories.newInstance(config.getPassword(),config.getMethod());
                            ChannelPipeline pipe = ch.pipeline();

                            pipe.addLast(new SSCipherCodec(cipher));
                            pipe.addLast(new SSTCPAddressDecoder());
                            pipe.addLast(new SSServerTCPRelayHandler(config));
                        }
                    });
            try {
                logger.info("ss-server listen on tcp {}:{}",config.getServer(),config.getServerPort());
                bootstrap.bind(config.getServer(),config.getServerPort()).sync();
            } catch (InterruptedException e) {
                logger.error(e.getMessage(),e);
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

    private class SSServerUDPServer implements Server {
        private EventLoopGroup acceptor = new NioEventLoopGroup();

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
                            SSCipher cipher = SSCipherFactories.newInstance(config.getPassword(),config.getMethod());
                            ChannelPipeline pipe = ctx.pipeline();
                            pipe.addLast(new SSUDPSessionCodec());
                            pipe.addLast(new SSCipherCodec(cipher,false));
                            pipe.addLast(new SSAddressingCodec());
                            pipe.addLast(new SSServerUDPRelayHandler());
                        }
                    });
            try {
                logger.info("ss-server listen on udp {}:{}",config.getServer(),config.getServerPort());
                boot.bind(config.getServer(), config.getServerPort()).sync();
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
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
