package org.wiyi.ss;

import org.wiyi.ss.core.SSConfig;
import org.wiyi.ss.core.config.SSConfigFactory;

public class SSLocalTest {
    public static void main(String[] args) {
        SSConfig config = new SSConfig();
        config.setTimeout(30);
        config.setLocalAddress("127.0.0.1");
        config.setLocalPort(1085);
        config.setServer("127.0.0.1");
        config.setServer("8388");
        config.setMethod("aes-128-gcm");
        config.setPassword("123456");

        SSLocal local = new SSLocal(config);
        local.open();
    }
}
