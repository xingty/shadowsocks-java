package org.wiyi.ss.net.udp;

import io.netty.channel.Channel;
import org.wiyi.ss.core.LRUCache;


public class UDPConnectionHolder extends LRUCache<String, Channel> {
    public static final UDPConnectionHolder HOLDER = new UDPConnectionHolder();

    public UDPConnectionHolder() {
        super(30);
    }

    public void register(String key, Channel channel) {
        put(key,channel);
    }

    public Channel getChannel(String key) {
        return get(key);
    }

}
