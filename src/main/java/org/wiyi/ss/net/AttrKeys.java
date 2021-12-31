package org.wiyi.ss.net;

import io.netty.util.AttributeKey;
import org.wiyi.ss.core.SSRequest;

import java.net.InetSocketAddress;

public class AttrKeys {
    public static final AttributeKey<SSRequest> ADDRESSING_FLIGHT = AttributeKey.valueOf("ADDRESSING_FLIGHT");
    public static final AttributeKey<SSRequest> ADDRESS_LAND = AttributeKey.valueOf("ADDRESS_LAND");

    public static final AttributeKey<InetSocketAddress> UDP_SENDER = AttributeKey.valueOf("UDP_SENDER");
}
