package az.aladdin.emaildelivery.exception;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * Resolves message keys against {@code messages*.properties}, falling back to English and finally to the raw key.
 * Arguments prefixed with {@code entity.} are themselves localized before being interpolated.
 */
@Component
@RequiredArgsConstructor
public class LocalizedMessageResolver {

    private static final String ENTITY_PREFIX = "entity.";

    private final MessageSource messageSource;

    public String resolve(String messageKey, Object[] args, Locale locale) {
        Object[] resolvedArgs = resolveArgs(args, locale);
        try {
            return messageSource.getMessage(messageKey, resolvedArgs, locale);
        } catch (NoSuchMessageException ignored) {
            try {
                return messageSource.getMessage(messageKey, resolvedArgs, Locale.ENGLISH);
            } catch (NoSuchMessageException ignored2) {
                return messageKey;
            }
        }
    }

    private Object[] resolveArgs(Object[] args, Locale locale) {
        if (args == null || args.length == 0) {
            return args;
        }
        Object[] out = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            Object a = args[i];
            if (a instanceof String s && s.startsWith(ENTITY_PREFIX)) {
                try {
                    out[i] = messageSource.getMessage(s, null, locale);
                } catch (NoSuchMessageException e) {
                    try {
                        out[i] = messageSource.getMessage(s, null, Locale.ENGLISH);
                    } catch (NoSuchMessageException e2) {
                        out[i] = s;
                    }
                }
            } else {
                out[i] = a;
            }
        }
        return out;
    }
}
