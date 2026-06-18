package az.aladdin.emaildelivery.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ExceptionConstants {

    UNEXPECTED_EXCEPTION("UNEXPECTED_EXCEPTION", "Unexpected exception with method: %s"),
    NOT_FOUND("NOT_FOUND", "%s not found"),
    ALREADY_EXISTS("ALREADY_EXISTS", "%s already exists"),
    BAD_EXCEPTION("BAD_EXCEPTION", "Bad exception: %s"),
    UNAUTHORIZED("UNAUTHORIZED", "No authenticated user found"),
    FORBIDDEN("FORBIDDEN", "Not allowed");

    private final String code;
    private final String message;

    public String formatMessage(Object... args) {
        return String.format(message, args);
    }

    public String formatCode(String entity) {
        return code + "_" + entity.toUpperCase();
    }
}
