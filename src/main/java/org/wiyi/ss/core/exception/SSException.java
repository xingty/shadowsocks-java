package org.wiyi.ss.core.exception;

public class SSException extends RuntimeException{

    /**
     * @param msg the detail message.
     */
    public SSException(String msg) {
        super(msg);
    }

    /**
     * @param msg the detail message.
     * @param cause the nested exception.
     */
    public SSException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
