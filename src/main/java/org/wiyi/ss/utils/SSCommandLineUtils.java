package org.wiyi.ss.utils;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class SSCommandLineUtils {
    public static Options getCommonOptions() {
        Options options = new Options();
        options.addOption(Option.builder("c")
                .longOpt("config")
                .hasArg(true)
                .desc("Configuration file")
                .valueSeparator(' ')
                .required(false)
                .build());

        options.addOption(Option.builder("m")
                .longOpt("method")
                .hasArg(true)
                .desc("Cipher method. ['rc4-md5', 'aes-128/192/256-cfb/ctr','aes-128/256-gcm','chacha20-ietf-poly1305']")
                .valueSeparator(' ')
                .required(false)
                .build());

        options.addOption(Option.builder("k")
                .longOpt("password")
                .hasArg(true)
                .desc("Password")
                .valueSeparator(' ')
                .required(false)
                .build());

        options.addOption(Option.builder("t")
                .longOpt("timeout")
                .hasArg(true)
                .desc("Connection timeout")
                .valueSeparator(' ')
                .required(false)
                .build());

        options.addOption(Option.builder("s")
                .longOpt("server-addr")
                .hasArg(true)
                .desc("Server address. (format: host:port)")
                .valueSeparator(' ')
                .required(false)
                .build());

        //flags
        options.addOption(Option.builder("h")
                .longOpt("help")
                .hasArg(false)
                .desc("Prints help info")
                .required(false)
                .build());

        options.addOption(Option.builder("v")
                .longOpt("version")
                .hasArg(false)
                .desc("Show version number")
                .required(false)
                .build());

        return options;
    }

    public static Options ssLocalOptions() {
        Options options = getCommonOptions();
        options.addOption(Option.builder("b")
                .longOpt("local-addr")
                .hasArg(true)
                .desc("Local address, listen only to this address if specified. (format: host:port)")
                .valueSeparator(' ')
                .required(false)
                .build());

        return options;
    }

    public static Options ssServerOptions() {
        Options options = getCommonOptions();

        options.addOption(Option.builder("b")
                .longOpt("outbound-bind-addr")
                .hasArg(true)
                .desc("Bind address")
                .valueSeparator(' ')
                .required(false)
                .build());

        options.addOption(Option.builder("up")
                .longOpt("udp-port")
                .hasArg(true)
                .desc("UDP port")
                .valueSeparator(' ')
                .required(false)
                .build());

        return options;
    }
}
