package az.aladdin.emaildelivery.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.List;
import java.util.Locale;

@Configuration
public class LocaleConfiguration {

    /**
     * Resolves locale from the {@code Accept-Language} header. Supported: English (default), Azerbaijani, Russian,
     * Georgian — matching the languages offered by stay-board (PMS) and stay-board-rms (RMS).
     */
    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver();
        resolver.setDefaultLocale(Locale.ENGLISH);
        resolver.setSupportedLocales(List.of(
                Locale.ENGLISH,
                Locale.forLanguageTag("az"),
                Locale.forLanguageTag("ru"),
                Locale.forLanguageTag("ka")
        ));
        return resolver;
    }
}
