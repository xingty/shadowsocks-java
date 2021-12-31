package org.wiyi.ss.core.config;

import io.netty.util.internal.StringUtil;
import org.apache.commons.cli.CommandLine;
import org.wiyi.ss.core.SSConfig;
import org.wiyi.ss.core.exception.SSException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class SSConfigFactory {
    public static SSConfig loadFromPath(String path) {
        try {
            byte[] bytes = Files.readAllBytes(Paths.get(path));
            SSJSONObject obj = new SSJSONObject(new String(bytes));

            return build(obj);
        } catch (IOException e) {
            throw new SSException(e.getMessage());
        }
    }

    private static SSConfig build(SSJSONObject obj) {
        String server = obj.getString(SSConfig.SERVER,"0.0.0.0");
        int serverPort = obj.getInt(SSConfig.SERVER_PORT,8383);
        String password = obj.getString(SSConfig.PASSWORD);
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("password must not be empty");
        }

        String method = obj.getString(SSConfig.METHOD,"aes-128-gcm");
        String localAddress = obj.getString(SSConfig.LOCAL_ADDRESS,"127.0.0.1");
        int localPort = obj.getInt(SSConfig.LOCAL_PORT,1080);
        int timeout = obj.getInt(SSConfig.TIMEOUT,SSConfig.DEFAULT_TIMEOUT);

        boolean enableUDP = obj.getBoolean(SSConfig.UDP,false);
        int udpPort = obj.getInt(SSConfig.UDP_PORT,0);

        SSConfig config = new SSConfig();
        config.setServer(server);
        config.setServerPort(serverPort);
        config.setPassword(password);
        config.setMethod(method);
        config.setLocalAddress(localAddress);
        config.setLocalPort(localPort);
        config.setUdp(enableUDP);
        config.setUdpPort(udpPort);
        config.setTimeout(timeout);

        return config;
    }

    public static SSConfig loadFromCommandLine(CommandLine cl) {
        SSConfig config = new SSConfig();
        if (cl.hasOption("c")) {
            String path = cl.getOptionValue("c");
            config = loadFromPath(path);
        }

        if (cl.hasOption("k") && !StringUtil.isNullOrEmpty(cl.getOptionValue("k"))) {
            config.setPassword(cl.getOptionValue("k"));
        }

        if (cl.hasOption("m") && !StringUtil.isNullOrEmpty(cl.getOptionValue("m"))) {
            config.setMethod(cl.getOptionValue("m"));
        }

        if (cl.hasOption("s")) {
            String serverAddr = cl.getOptionValue("s");
            if (!StringUtil.isNullOrEmpty(serverAddr)) {
                String[] arr = serverAddr.split(":");
                config.setServer(arr[0]);
                config.setServerPort(Integer.parseInt(arr[1]));
            }
        }

        if (cl.hasOption("t") && !StringUtil.isNullOrEmpty(cl.getOptionValue("t"))) {
            String timeout = cl.getOptionValue("t");
            config.setTimeout(Integer.parseInt(timeout));
        }

        if (cl.hasOption("b")) {
            String addr = cl.getOptionValue("b");
            String[] arr = addr.split(":");
            config.setLocalAddress(arr[0]);
            config.setLocalPort(Integer.parseInt(arr[1]));
        }

        return config;
    }
}
