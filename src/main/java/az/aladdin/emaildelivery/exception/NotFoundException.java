package az.aladdin.emaildelivery.exception;

import lombok.Getter;

@Getter
public class NotFoundException extends RuntimeException {
    private final String code;
    private final String messageKey;
    private final transient Object[] args;

    public NotFoundException(String code, String messageKey, Object... args) {
        super(messageKey);
        this.code = code;
        this.messageKey = messageKey;
        this.args = (args != null && args.length > 0) ? args.clone() : null;
    }
}
