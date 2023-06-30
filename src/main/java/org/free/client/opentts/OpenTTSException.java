package org.free.client.opentts;

/**
 * OpenTTS common exception
 *
 * @author bayura-ea
 */
public class OpenTTSException extends RuntimeException {

    public OpenTTSException() {
        super();
    }

    public OpenTTSException(String message) {
        super(message);
    }

    public OpenTTSException(String message, Throwable cause) {
        super(message, cause);
    }

    public OpenTTSException(Throwable cause) {
        super(cause);
    }

    protected OpenTTSException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
