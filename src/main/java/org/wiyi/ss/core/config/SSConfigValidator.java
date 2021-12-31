package org.wiyi.ss.core.config;

import io.netty.util.internal.StringUtil;
import org.wiyi.ss.core.SSConfig;
import org.wiyi.ss.core.exception.SSConfigValidateException;

public class SSConfigValidator {

    public static void validate(SSConfig config, boolean isSSLocal) {
        if (StringUtil.isNullOrEmpty(config.getMethod())) {
            throw new SSConfigValidateException("cipher method can not be empty.");
        }

        if (StringUtil.isNullOrEmpty(config.getPassword())) {
            throw new SSConfigValidateException("password can not be empty.");
        }

        if (StringUtil.isNullOrEmpty(config.getServer())) {
            throw new SSConfigValidateException("server can not be empty");
        }

        if (isSSLocal) {
            validSSLocal(config);
        } else {
            validSSServer(config);
        }
    }

    private static void validSSLocal(SSConfig config) {

    }

    private static void validSSServer(SSConfig config) {

    }
}
