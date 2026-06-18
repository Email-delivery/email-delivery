package az.aladdin.emaildelivery.email;

import az.aladdin.emaildelivery.config.MailConfig;
import az.aladdin.emaildelivery.model.entity.AdminMailConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class AdminMailSenderResolver {

    private final JavaMailSender defaultMailSender;
    private final MailConfig mailConfig;

    public boolean hasCustomSmtpConfig(AdminMailConfig config) {
        return config != null
                && config.getPort() != null
                && StringUtils.hasText(config.getHost())
                && StringUtils.hasText(config.getUsername())
                && StringUtils.hasText(config.getPassword());
    }

    public JavaMailSender resolveMailSender(AdminMailConfig config) {
        if (!hasCustomSmtpConfig(config)) {
            return defaultMailSender;
        }
        return mailConfig.createMailSender(
                config.getHost().trim(),
                config.getPort(),
                config.getUsername().trim(),
                config.getPassword());
    }

    public String resolveFrom(JavaMailSender mailSender, AdminMailConfig config) {
        if (hasCustomSmtpConfig(config)) {
            return config.getUsername().trim();
        }
        return ((JavaMailSenderImpl) mailSender).getUsername();
    }
}
