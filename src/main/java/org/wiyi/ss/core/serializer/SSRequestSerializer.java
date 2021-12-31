package org.wiyi.ss.core.serializer;

import io.netty.handler.codec.socks.SocksAddressType;
import io.netty.handler.codec.socksx.v5.Socks5AddressType;
import io.netty.util.NetUtil;
import org.bouncycastle.util.Arrays;
import org.wiyi.ss.core.SSRequest;
import org.wiyi.ss.core.exception.SSSerializationException;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class SSRequestSerializer implements SSSerializer<SSRequest> {

    public static final SSRequestSerializer INSTANCE = new SSRequestSerializer();

    @Override
    public byte[] serialize(SSRequest req) throws SSSerializationException {
        int length = calcRequestLength(req);
        ByteBuffer buffer = ByteBuffer.allocate(length);
        int type = req.getType();

        //[1-byte type]
        buffer.put(req.getType());
        boolean isIPAddress = type == Socks5AddressType.IPv4.byteValue()
                || type == Socks5AddressType.IPv6.byteValue();
        if (isIPAddress) {
            byte[] bytes = NetUtil.createByteArrayFromIpAddressString(req.getHost());
            buffer.put(bytes);
        } else if (type == Socks5AddressType.DOMAIN.byteValue()) {
            String host = req.getHost();
            int hostLen = host.length();
            if (hostLen > 256) {
                throw new SSSerializationException(
                        "Cannot serialize: the length of hostname must be less than 256; hostname: [" + host +"]");
            }

            buffer.put((byte) hostLen);
            buffer.put(host.getBytes(StandardCharsets.UTF_8));
        } else {
            throw new SSSerializationException("invalid request type: {"+ type + "}");
        }

        buffer.putShort((short) req.getPort());

        if (!Arrays.isNullOrEmpty(req.getData())) {
            buffer.put(req.getData());
        }

        return buffer.array();
    }

    @Override
    public SSRequest deserialize(byte[] bytes) throws SSSerializationException {
        int len = bytes.length;
        if (len < SSRequest.MIN_LENGTH) {
            throw new SSSerializationException("deserialize fail: invalid data length: {" + len + "}");
        }

        ByteBuffer buf = ByteBuffer.wrap(bytes);
        SSRequest request = new SSRequest();
        byte type = buf.get();
        String host = decodeHost(type,buf);
        int port = buf.getShort() & 0xFFFF; //UnsignedShort

        request.setType(type);
        request.setHost(host);
        request.setPort(port);

        int index = buf.position();
        if (index != len) {
            byte[] data = new byte[len - index];
            buf.get(data);
            request.setData(data);
        }

        return request;
    }

    private int calcRequestLength(SSRequest req) {
        byte[] data = req.getData();
        int dataLen = data == null ? 0 : data.length;
        int hostLen;
        /*
         * The following address types are defined:
         * • 0x01: host is a 4-byte IPv4 address.
         * • 0x03: host is a variable length string, starting with a 1-byte length,followed by up to 255-byte domain name.
         * • 0x04: host is a 16-byte IPv6 address.
         */
        int type = req.getType();
        if (type == SocksAddressType.IPv4.byteValue()) {
            hostLen = 4;
        } else if (type == SocksAddressType.IPv6.byteValue()) {
            hostLen = 16;
        } else if (type == SocksAddressType.DOMAIN.byteValue()) {
            //[1-byte length][variable length domain]
            hostLen = req.getHost().length() + 1;
        } else {
            throw new SSSerializationException("invalid request type: {"+ type + "}");
        }

        //target addresses: [1-byte type][variable-length host][2-byte port]
        //request = [target addresses][payload]
        return 1 + hostLen + 2 + dataLen;
    }

    /**
     * decode host from buffer
     */
    private String decodeHost(byte type,ByteBuffer bytes) {
        if (type == Socks5AddressType.IPv4.byteValue()) {
            return NetUtil.intToIpAddress(bytes.getInt());
        } else if (type == Socks5AddressType.IPv6.byteValue()) {
            byte[] address = new byte[16];
            bytes.get(address);
            return NetUtil.bytesToIpAddress(address);
        } else if (type == Socks5AddressType.DOMAIN.byteValue()) {
            byte domainLen = bytes.get();
            byte[] domain = new byte[domainLen];
            bytes.get(domain);
            return new String(domain, StandardCharsets.UTF_8);
        } else {
            //The value of type may be changed by attacker(GFW).
            throw new SSSerializationException("invalid request type: {"+ type + "}");
        }
    }
}
