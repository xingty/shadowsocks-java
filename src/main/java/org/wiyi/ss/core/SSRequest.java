package org.wiyi.ss.core;

import java.io.Serializable;
import java.util.Arrays;

public class SSRequest implements Serializable {


    public static final int MIN_LENGTH = 7;

    /**
     * The following address types are defined:
     * • 0x01: host is a 4-byte IPv4 address.
     * • 0x03: host is a variable length string, starting with a 1-byte length,followed by up to 255-byte domain name.
     * • 0x04: host is a 16-byte IPv6 address.
     */
    private byte type;
    private String host;

    /**
     * The port number is a 2-byte big-endian unsigned integer.
     */
    private int port;

    private byte[] data;

    public SSRequest(){}

    public SSRequest(byte type, String host, int port) {
        this.type = type;
        this.host = host;
        this.port = port;
    }

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "{" +
                "type=" + type +
                ", host='" + host + '\'' +
                ", port=" + port +
                ", data=" + Arrays.toString(data) +
                '}';
    }
}
