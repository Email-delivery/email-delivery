package az.aladdin.emaildelivery.exception;

import lombok.Getter;

@Getter
public class BadException extends RuntimeException {
    private final String code;
    private final String messageKey;
    private final transient Object[] args;

    public BadException(String code, String messageKey) {
        super(messageKey);
        this.code = code;
        this.messageKey = messageKey;
        this.args = null;
    }

    public BadException(String code, String messageKey, Object... args) {
        super(messageKey);
        this.code = code;
        this.messageKey = messageKey;
        this.args = args.length > 0 ? args.clone() : null;
    }
}
