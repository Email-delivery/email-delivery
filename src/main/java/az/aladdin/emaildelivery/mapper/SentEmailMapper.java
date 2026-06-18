package az.aladdin.emaildelivery.mapper;

import az.aladdin.emaildelivery.model.entity.SentEmail;
import az.aladdin.emaildelivery.model.entity.SentEmailRecipient;
import az.aladdin.emaildelivery.model.enums.EmailRecipientType;
import az.aladdin.emaildelivery.model.response.email.EmailRecipientStatusResponse;
import az.aladdin.emaildelivery.model.response.email.SentEmailResponse;
import az.aladdin.emaildelivery.email.EmailFromAddress;
import az.aladdin.emaildelivery.util.InstantFormatting;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Locale;

@Component
public class SentEmailMapper {

    public SentEmailResponse toResponse(SentEmail email) {
        return SentEmailResponse.builder()
                .id(String.valueOf(email.getId()))
                .subject(email.getSubject())
                .bodyHtml(email.getBodyHtml())
                .sentBy(email.getSentByEmail())
                .sentByName(email.getSentByName())
                .fromEmail(email.getFromEmail())
                .fromName(email.getFromName())
                .fromLabel(formatFromLabel(email.getFromName(), email.getFromEmail()))
                .sentAt(formatInstant(email.getSentAt()))
                .scheduledAt(formatInstant(email.getScheduledAt()))
                .status(email.getStatus().name().toLowerCase(Locale.ROOT))
                .to(mapRecipients(email, EmailRecipientType.TO))
                .cc(mapRecipients(email, EmailRecipientType.CC))
                .bcc(mapRecipients(email, EmailRecipientType.BCC))
                .build();
    }

    private List<EmailRecipientStatusResponse> mapRecipients(SentEmail email, EmailRecipientType type) {
        return email.getRecipients().stream()
                .filter(r -> r.getRecipientType() == type)
                .map(this::toRecipientResponse)
                .toList();
    }

    private EmailRecipientStatusResponse toRecipientResponse(SentEmailRecipient recipient) {
        return EmailRecipientStatusResponse.builder()
                .email(recipient.getEmail())
                .name(recipient.getName())
                .status(recipient.getStatus().name().toLowerCase())
                .deliveredAt(formatInstant(recipient.getDeliveredAt()))
                .openedAt(formatInstant(recipient.getOpenedAt()))
                .errorMessage(recipient.getErrorMessage())
                .build();
    }

    private String formatInstant(Instant instant) {
        return InstantFormatting.toApiString(instant);
    }

    private String formatFromLabel(String fromName, String fromEmail) {
        if (fromEmail == null || fromEmail.isBlank()) {
            return null;
        }
        return EmailFromAddress.formatLabel(fromName, fromEmail);
    }
}
