package net.cz88.czdb.exception;

public class IpFormatException extends Exception {
    public IpFormatException() {
    }

    public IpFormatException(String message) {
        super(message);
    }

    public IpFormatException(String message, Throwable cause) {
        super(message, cause);
    }

    public IpFormatException(Throwable cause) {
        super(cause);
    }

    public IpFormatException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
