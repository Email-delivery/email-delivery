package az.aladdin.emaildelivery.service.email;

import az.aladdin.emaildelivery.model.entity.SentEmail;
import az.aladdin.emaildelivery.model.entity.SentEmailRecipient;
import az.aladdin.emaildelivery.model.enums.EmailDeliveryStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

@Component
public class AdminEmailStatusResolver {

    public EmailDeliveryStatus computeAggregateStatus(List<SentEmailRecipient> recipients) {
        if (recipients == null || recipients.isEmpty()) {
            return EmailDeliveryStatus.QUEUED;
        }

        var statuses = recipients.stream().map(SentEmailRecipient::getStatus).toList();

        if (statuses.stream().allMatch(status -> status == EmailDeliveryStatus.QUEUED)) {
            return EmailDeliveryStatus.QUEUED;
        }

        if (statuses.stream().allMatch(status -> status == EmailDeliveryStatus.FAILED
                || status == EmailDeliveryStatus.BOUNCED)) {
            return EmailDeliveryStatus.FAILED;
        }

        if (statuses.stream().allMatch(status -> status == EmailDeliveryStatus.OPENED)) {
            return EmailDeliveryStatus.OPENED;
        }

        if (statuses.stream().anyMatch(status -> status == EmailDeliveryStatus.OPENED)) {
            return EmailDeliveryStatus.OPENED;
        }

        if (statuses.stream().anyMatch(status -> status == EmailDeliveryStatus.FAILED
                || status == EmailDeliveryStatus.BOUNCED)) {
            return EmailDeliveryStatus.FAILED;
        }

        if (statuses.stream().allMatch(status -> status == EmailDeliveryStatus.DELIVERED
                || status == EmailDeliveryStatus.OPENED
                || status == EmailDeliveryStatus.SENT)) {
            return EmailDeliveryStatus.DELIVERED;
        }

        return EmailDeliveryStatus.SENT;
    }

    public void applyAggregateStatus(SentEmail email) {
        email.setStatus(computeAggregateStatus(email.getRecipients()));
    }

    public void markDelivered(SentEmailRecipient recipient, java.time.Instant deliveredAt) {
        recipient.setStatus(EmailDeliveryStatus.DELIVERED);
        recipient.setDeliveredAt(deliveredAt);
        recipient.setErrorMessage(null);
    }

    public void markFailed(SentEmailRecipient recipient, String message) {
        recipient.setStatus(EmailDeliveryStatus.FAILED);
        recipient.setErrorMessage(truncate(message, 500));
    }

    public void markOpened(SentEmailRecipient recipient, java.time.Instant openedAt) {
        recipient.setOpenedAt(openedAt);
        recipient.setStatus(EmailDeliveryStatus.OPENED);
        if (recipient.getDeliveredAt() == null) {
            recipient.setDeliveredAt(openedAt);
        }
    }

    public boolean matchesEmail(SentEmailRecipient left, SentEmailRecipient right) {
        return left.getEmail().trim().equalsIgnoreCase(right.getEmail().trim())
                && left.getRecipientType() == right.getRecipientType();
    }

    private String truncate(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}
