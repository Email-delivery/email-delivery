package az.aladdin.emaildelivery.exception;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Translates exceptions into localized, user-friendly {@link ApiErrorResponse} payloads.
 *
 * <p>Guiding principles:
 * <ul>
 *     <li>Never leak stack traces or internal identifiers to API consumers.</li>
 *     <li>Every response carries a stable machine-readable {@code code} and a localized {@code message}.</li>
 *     <li>Failures from external services are surfaced with their HTTP status when available.</li>
 * </ul>
 */
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final LocalizedMessageResolver localizedMessageResolver;

    @ExceptionHandler(ExpiredRefreshTokenException.class)
    public ResponseEntity<ApiErrorResponse> handleExpiredRefreshToken(ExpiredRefreshTokenException ex) {
        return localized(HttpStatus.UNAUTHORIZED, ex.getCode(), ex.getMessageKey(), ex.getArgs());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        String messageKey = ex.getMessage() != null ? ex.getMessage() : MessageKeys.EXCEPTION_BAD_REQUEST;
        return localized(HttpStatus.BAD_REQUEST, EntityNames.BAD_REQUEST, messageKey, null);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(NotFoundException ex) {
        return localized(HttpStatus.NOT_FOUND, ex.getCode(), ex.getMessageKey(), ex.getArgs());
    }

    @ExceptionHandler(BadException.class)
    public ResponseEntity<ApiErrorResponse> handleBad(BadException ex) {
        return localized(HttpStatus.BAD_REQUEST, ex.getCode(), ex.getMessageKey(), ex.getArgs());
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiErrorResponse> handleUnauthorized(UnauthorizedException ex) {
        return localized(HttpStatus.UNAUTHORIZED, ex.getCode(), ex.getMessageKey(), ex.getArgs());
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiErrorResponse> handleForbidden(ForbiddenException ex) {
        return localized(HttpStatus.FORBIDDEN, ex.getCode(), ex.getMessageKey(), ex.getArgs());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        Locale locale = LocaleContextHolder.getLocale();
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(GlobalExceptionHandler::formatFieldError)
                .collect(Collectors.joining("; "));
        String message = localizedMessageResolver.resolve(MessageKeys.EXCEPTION_VALIDATION_FAILED, null, locale);
        if (!details.isBlank()) {
            message = message + ": " + details;
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiErrorResponse(MessageKeys.EXCEPTION_VALIDATION_FAILED, message));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraint(ConstraintViolationException ex) {
        Locale locale = LocaleContextHolder.getLocale();
        String details = ex.getConstraintViolations() == null ? "" : ex.getConstraintViolations().stream()
                .map(v -> leafNode(v.getPropertyPath()) + ": " + v.getMessage())
                .collect(Collectors.joining("; "));
        String message = localizedMessageResolver.resolve(MessageKeys.EXCEPTION_VALIDATION_FAILED, null, locale);
        if (!details.isBlank()) {
            message = message + ": " + details;
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiErrorResponse(MessageKeys.EXCEPTION_VALIDATION_FAILED, message));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleNotReadable(HttpMessageNotReadableException ex) {
        log.debug("Malformed request body: {}", ex.getMessage());
        return localized(HttpStatus.BAD_REQUEST, EntityNames.MALFORMED_REQUEST,
                MessageKeys.EXCEPTION_MALFORMED_REQUEST, null);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingParam(MissingServletRequestParameterException ex) {
        return localized(HttpStatus.BAD_REQUEST, EntityNames.MISSING_PARAMETER,
                MessageKeys.EXCEPTION_MISSING_PARAMETER, new Object[]{ex.getParameterName()});
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        log.debug("Argument type mismatch for '{}': {}", ex.getName(), ex.getMessage());
        return localized(HttpStatus.BAD_REQUEST, EntityNames.INVALID_FIELD_VALUE,
                MessageKeys.EXCEPTION_INVALID_FIELD_VALUE, null);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        return localized(HttpStatus.METHOD_NOT_ALLOWED, EntityNames.METHOD_NOT_ALLOWED,
                MessageKeys.EXCEPTION_METHOD_NOT_ALLOWED, null);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex) {
        return localized(HttpStatus.UNSUPPORTED_MEDIA_TYPE, EntityNames.BAD_REQUEST,
                MessageKeys.EXCEPTION_MALFORMED_REQUEST, null);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNoResource(NoResourceFoundException ex) {
        return localized(HttpStatus.NOT_FOUND, EntityNames.NOT_FOUND_RESOURCE,
                MessageKeys.EXCEPTION_RESOURCE_NOT_FOUND, null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        return localized(HttpStatus.FORBIDDEN, EntityNames.ACCESS_DENIED,
                MessageKeys.EXCEPTION_ACCESS_DENIED, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception ex) {
        log.error("Unexpected exception", ex);
        return localized(HttpStatus.INTERNAL_SERVER_ERROR, EntityNames.INTERNAL_ERROR,
                MessageKeys.EXCEPTION_INTERNAL_ERROR, null);
    }

    private ResponseEntity<ApiErrorResponse> localized(HttpStatus status, String code, String messageKey,
                                                       Object[] args) {
        Locale locale = LocaleContextHolder.getLocale();
        String message = localizedMessageResolver.resolve(messageKey, args, locale);
        return ResponseEntity.status(status).body(new ApiErrorResponse(code, message));
    }

    private static String leafNode(Path propertyPath) {
        if (propertyPath == null) {
            return "";
        }
        String full = propertyPath.toString();
        int lastDot = full.lastIndexOf('.');
        return lastDot >= 0 ? full.substring(lastDot + 1) : full;
    }

    private static String formatFieldError(FieldError e) {
        return e.getField() + ": " + e.getDefaultMessage();
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}
