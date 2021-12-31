package org.wiyi.ss.utils;

import io.netty.handler.codec.socksx.v5.Socks5AddressType;
import io.netty.handler.codec.socksx.v5.Socks5CommandRequest;
import org.wiyi.ss.core.SSRequest;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;

public class SSRequestUtils {
    public static SSRequest getFromSocksRequest(Socks5CommandRequest request) {
        SSRequest ssr = new SSRequest();
        ssr.setType(request.dstAddrType().byteValue());
        ssr.setHost(request.dstAddr());
        ssr.setPort(request.dstPort());

        return ssr;
    }

    public static SSRequest getFromInetAddress(InetSocketAddress address) {
        SSRequest request = new SSRequest();
        request.setHost(address.getHostString());
        request.setPort(address.getPort());
        Socks5AddressType type = (address.getAddress() instanceof Inet4Address) ?
                Socks5AddressType.IPv4 : Socks5AddressType.IPv6;
        request.setType(type.byteValue());

        return request;
    }
}
