package org.wiyi.ss.codec;

import org.junit.Assert;
import org.junit.Test;
import org.wiyi.ss.core.SSRequest;
import org.wiyi.ss.core.serializer.SSRequestSerializer;
import org.wiyi.ss.utils.ArrayUtils;

import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class SSRequestTest {

    SSRequestSerializer serializer = new SSRequestSerializer();


//    @Test
//    public void testFile() throws Exception{
//        SSRequest request = new SSRequest();
//        request.setType((byte) 1);
//        request.setHost("127.0.0.1");
//        request.setPort(1086);
//        request.setData("123456789".getBytes(StandardCharsets.UTF_8));
//        byte[] data = serializer.serialize(request);
//
//        byte[] prefix = new byte[3];
//        prefix[2] = 1;
//
//        FileOutputStream fos = new FileOutputStream("/Users/bigbyto/Program/udp.datagram");
//        fos.write(ArrayUtils.merge(prefix,data));
//    }

    @Test
    public void testSerializerWithoutPayload() {
        SSRequest request = new SSRequest();
        request.setType((byte) 1);
        request.setHost("127.0.0.1");
        request.setPort(80);

        byte[] data = serializer.serialize(request);
        Assert.assertEquals(7,data.length);
    }

    @Test
    public void testSerializer() {
        byte[] except = new byte[]{1, -64, -88, 1, 1, 1, -69, 49, 50, 51, 52, 53, 54};
        byte[] payload = "123456".getBytes(StandardCharsets.UTF_8);
        System.out.println(Arrays.toString(payload));
        SSRequest req = new SSRequest();
        req.setType((byte) 1);
        req.setPort(443);
        req.setHost("192.168.1.1");
        req.setData(payload);

        byte[] encodedData = serializer.serialize(req);
        System.out.println("encode data: " + Arrays.toString(encodedData));
        Assert.assertArrayEquals(except,encodedData);
    }

    @Test
    public void testDeserializer(){
        byte[] source = new byte[]{1, -64, -88, 1, 1, 1, -69, 49, 50, 51, 52, 53, 54};
        SSRequest req = serializer.deserialize(source);
        System.out.println("decode data: " + req);

        Assert.assertEquals(1,req.getType());
        Assert.assertEquals("192.168.1.1",req.getHost());
        Assert.assertEquals(443,req.getPort());
        byte[] exceptBytes = "123456".getBytes(StandardCharsets.UTF_8);
        Assert.assertArrayEquals(exceptBytes,req.getData());
    }
}
