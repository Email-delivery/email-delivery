package az.aladdin.emaildelivery.service.auth;

import az.aladdin.emaildelivery.annotation.NoLogging;
import az.aladdin.emaildelivery.email.AdminMailSenderResolver;
import az.aladdin.emaildelivery.model.enums.OtpPurpose;
import az.aladdin.emaildelivery.service.email.AdminMailConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@NoLogging
@RequiredArgsConstructor
public class AdminOtpEmailService {

    private final AdminMailConfigService adminMailConfigService;
    private final AdminMailSenderResolver adminMailSenderResolver;

    public void sendOtp(String email, String recipientName, String code, OtpPurpose purpose) {
        String subject = resolveSubject(purpose);
        String body = """
                Hello %s,

                Your verification code is: %s

                This code expires in 5 minutes.

                If you did not request this, please ignore this email.
                """.formatted(recipientName != null ? recipientName : "Admin", code);

        var config = adminMailConfigService.findActiveConfig();
        var mailSender = adminMailSenderResolver.resolveMailSender(config);
        var from = adminMailSenderResolver.resolveFrom(mailSender, config);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(email);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
        log.info("OTP email sent to {} for purpose {}", email, purpose);
    }

    private String resolveSubject(OtpPurpose purpose) {
        return switch (purpose) {
            case ACCOUNT_ACTIVATION -> "StayBoard Admin — Account activation";
            case PASSWORD_RESET -> "StayBoard Admin — Password reset";
        };
    }
}
