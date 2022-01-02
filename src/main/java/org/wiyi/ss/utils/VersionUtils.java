package org.wiyi.ss.utils;

import org.wiyi.ss.SSBootstrap;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class VersionUtils {
    private static final String UNKNOWN = "unknow";
    private static final String VERSION = "project.version";
    private static final String PROJECT_NAME = "project.name";

    static class VersionInfo {
        public final String name;
        public final String version;


        VersionInfo(String artifactId, String version) {
            this.name = artifactId;
            this.version = version;
        }

        @Override
        public String toString() {
            return name + " " + version;
        }
    }

    public static VersionInfo getVersion() {
        InputStream is = SSBootstrap.class.getClassLoader().getResourceAsStream("project.properties");
        if (is == null) { return new VersionInfo("shadowsocks-java",UNKNOWN); }

        try {
            Properties properties = new Properties();
            properties.load(is);

            String version = properties.getProperty(VERSION);
            String name = properties.getProperty(PROJECT_NAME);

            return new VersionInfo(name,version);
        } catch (IOException e) {
            return new VersionInfo("shadowsocks-java",UNKNOWN);
        }
    }
}
