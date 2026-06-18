package az.aladdin.emaildelivery.service.email;

import az.aladdin.emaildelivery.annotation.NoLogging;
import az.aladdin.emaildelivery.email.AdminMailSenderResolver;
import az.aladdin.emaildelivery.exception.BadException;
import az.aladdin.emaildelivery.exception.EntityNames;
import az.aladdin.emaildelivery.exception.MessageKeys;
import az.aladdin.emaildelivery.exception.NotFoundException;
import az.aladdin.emaildelivery.model.entity.AdminMailConfig;
import az.aladdin.emaildelivery.model.request.email.AdminMailConfigRequest;
import az.aladdin.emaildelivery.model.request.email.AdminMailTestEmailRequest;
import az.aladdin.emaildelivery.model.response.email.AdminMailConfigResponse;
import az.aladdin.emaildelivery.repository.AdminMailConfigRepository;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AdminMailConfigService {

    private final AdminMailConfigRepository adminMailConfigRepository;
    private final AdminMailSenderResolver adminMailSenderResolver;

    @Transactional(readOnly = true)
    public AdminMailConfigResponse getMailConfig() {
        return adminMailConfigRepository.findTopByOrderByIdAsc()
                .map(this::toResponse)
                .orElseThrow(() -> new NotFoundException(
                        EntityNames.MAIL_CONFIG,
                        MessageKeys.EXCEPTION_NOT_FOUND,
                        MessageKeys.ENTITY_MAIL_CONFIG));
    }

    @Transactional
    @NoLogging
    public AdminMailConfigResponse createMailConfig(AdminMailConfigRequest request) {
        if (adminMailConfigRepository.count() > 0) {
            throw new BadException(EntityNames.MAIL_CONFIG, MessageKeys.EXCEPTION_MAIL_CONFIG_ALREADY_EXISTS);
        }
        if (!StringUtils.hasText(request.getPassword())) {
            throw new BadException(EntityNames.MAIL_CONFIG, MessageKeys.EXCEPTION_MAIL_CONFIG_PASSWORD_REQUIRED);
        }

        var config = AdminMailConfig.builder()
                .host(request.getHost().trim())
                .port(request.getPort())
                .username(request.getUsername().trim())
                .password(request.getPassword())
                .build();

        return toResponse(adminMailConfigRepository.save(config));
    }

    @Transactional
    @NoLogging
    public AdminMailConfigResponse updateMailConfig(AdminMailConfigRequest request) {
        var config = adminMailConfigRepository.findTopByOrderByIdAsc()
                .orElseThrow(() -> new NotFoundException(
                        EntityNames.MAIL_CONFIG,
                        MessageKeys.EXCEPTION_NOT_FOUND,
                        MessageKeys.ENTITY_MAIL_CONFIG));

        config.setHost(request.getHost().trim());
        config.setPort(request.getPort());
        config.setUsername(request.getUsername().trim());
        if (StringUtils.hasText(request.getPassword())) {
            config.setPassword(request.getPassword());
        }

        return toResponse(adminMailConfigRepository.save(config));
    }

    @Transactional(readOnly = true)
    public void sendTestEmail(AdminMailTestEmailRequest request) {
        var config = adminMailConfigRepository.findTopByOrderByIdAsc()
                .orElseThrow(() -> new NotFoundException(
                        EntityNames.MAIL_CONFIG,
                        MessageKeys.EXCEPTION_NOT_FOUND,
                        MessageKeys.ENTITY_MAIL_CONFIG));

        var mailSender = adminMailSenderResolver.resolveMailSender(config);
        var from = adminMailSenderResolver.resolveFrom(mailSender, config);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(request.getEmail().trim());
            helper.setSubject("StayBoard Admin — SMTP test email");
            helper.setText(buildTestEmailBody(), true);
            mailSender.send(message);
        } catch (Exception e) {
            throw new BadException(EntityNames.MAIL_CONFIG, MessageKeys.EXCEPTION_MAIL_CONFIG_TEST_FAILED);
        }
    }

    @Transactional(readOnly = true)
    public AdminMailConfig findActiveConfig() {
        return adminMailConfigRepository.findTopByOrderByIdAsc().orElse(null);
    }

    private AdminMailConfigResponse toResponse(AdminMailConfig config) {
        return AdminMailConfigResponse.builder()
                .host(config.getHost())
                .port(config.getPort())
                .username(config.getUsername())
                .build();
    }

    private String buildTestEmailBody() {
        return """
                <p>Hello,</p>
                <p>This is a test email from <strong>StayBoard Admin</strong>.</p>
                <p>The system SMTP configuration is working correctly.</p>
                <p>If you did not request this test, you can safely ignore this message.</p>
                """;
    }
}
