package org.wiyi.ss.core;


public class SSConfig {
    public static final String SERVER = "server";
    public static final String SERVER_PORT = "server_port";
    public static final String LOCAL_ADDRESS = "local_address";
    public static final String LOCAL_PORT = "local_port";
    public static final String PASSWORD = "password";
    public static final String METHOD = "method";
    public static final String TIMEOUT = "timeout";
    public static final String UDP = "udp";
    public static final String UDP_PORT = "udp_port";
    public static final String FAST_OPEN = "fast_open";

    public static final String DEFAULT_METHOD = "aes-256-cfb";
    public static final int DEFAULT_TIMEOUT = 30;

    /**
     * 	the address your server listens
     */
    private String server;

    /**
     * 	server port
     */
    private int serverPort;

    /**
     * the address your local listens
     */
    private String localAddress;

    /**
     * local port(TCP)
     */
    private int localPort;

    /**
     * 	password used for encryption
     */
    private String password;

    /**
     * 	in seconds
     */
    private int timeout = DEFAULT_TIMEOUT;

    /**
     * default: "aes-256-cfb", see Encryption
     */
    private String method = DEFAULT_METHOD;

    /**
     * use TCP_FASTOPEN, true / false
     */
    private boolean fastOpen;

    /**
     * UDP support, true or false
     */
    private boolean udp;

    /**
     * local udp port, available when udp is true
     */
    private int udpPort;

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public String getLocalAddress() {
        return localAddress;
    }

    public void setLocalAddress(String localAddress) {
        this.localAddress = localAddress;
    }

    public int getLocalPort() {
        return localPort;
    }

    public void setLocalPort(int localPort) {
        this.localPort = localPort;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public boolean isFastOpen() {
        return fastOpen;
    }

    public void setFastOpen(boolean fastOpen) {
        this.fastOpen = fastOpen;
    }

    public boolean isUdp() {
        return udp;
    }

    public void setUdp(boolean udp) {
        this.udp = udp;
    }

    public int getUdpPort() {
        return udpPort;
    }

    public void setUdpPort(int udpPort) {
        this.udpPort = udpPort;
    }

    @Override
    public String toString() {
        return "SSConfig{" +
                "server='" + server + '\'' +
                ", serverPort=" + serverPort +
                ", localAddress='" + localAddress + '\'' +
                ", localPort=" + localPort +
                ", password='" + password + '\'' +
                ", timeout=" + timeout +
                ", method='" + method + '\'' +
                ", fastOpen=" + fastOpen +
                ", udp=" + udp +
                ", udpPort=" + udpPort +
                '}';
    }
}
