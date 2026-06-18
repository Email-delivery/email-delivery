package az.aladdin.emaildelivery.service.email;

import az.aladdin.emaildelivery.config.AdminEmailProperties;
import az.aladdin.emaildelivery.config.AsyncConfig;
import az.aladdin.emaildelivery.email.AdminEmailHtmlEnhancer;
import az.aladdin.emaildelivery.email.AdminHtmlEmailSender;
import az.aladdin.emaildelivery.email.AdminMailSenderResolver;
import az.aladdin.emaildelivery.email.EmailFromAddress;
import az.aladdin.emaildelivery.model.entity.AdminMailConfig;
import az.aladdin.emaildelivery.model.entity.SentEmail;
import az.aladdin.emaildelivery.model.entity.SentEmailAttachment;
import az.aladdin.emaildelivery.model.entity.SentEmailRecipient;
import az.aladdin.emaildelivery.model.enums.EmailDeliveryStatus;
import az.aladdin.emaildelivery.repository.SentEmailRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminEmailDispatchProcessor {

    private final AdminHtmlEmailSender adminHtmlEmailSender;
    private final SentEmailRepository sentEmailRepository;
    private final AdminMailConfigService adminMailConfigService;
    private final AdminMailSenderResolver adminMailSenderResolver;
    private final AdminEmailProperties adminEmailProperties;
    private final AdminEmailStatusResolver adminEmailStatusResolver;
    private final AdminEmailUnsubscribeService adminEmailUnsubscribeService;

    @Async(AsyncConfig.ADMIN_EMAIL_EXECUTOR)
    @Transactional
    public void dispatch(Long sentEmailId) {
        var email = sentEmailRepository.findById(sentEmailId).orElse(null);
        if (email == null) {
            log.warn("Sent email {} not found for dispatch", sentEmailId);
            return;
        }

        var attachments = email.getAttachments().stream()
                .map(this::toPayload)
                .toList();
        var now = Instant.now();
        var mailConfig = adminMailConfigService.findActiveConfig();
        var mailSender = adminMailSenderResolver.resolveMailSender(mailConfig);
        var from = resolveFromAddress(email, mailSender, mailConfig);

        for (var recipient : email.getRecipients()) {
            if (adminEmailUnsubscribeService.isSuppressed(recipient.getEmail())) {
                adminEmailStatusResolver.markFailed(recipient, "Recipient unsubscribed");
                continue;
            }

            ensureTrackingToken(recipient);
            ensureUnsubscribeToken(recipient, email.isIncludeUnsubscribe());

            var htmlBody = email.getBodyHtml();
            if (email.isIncludeUnsubscribe() && StringUtils.hasText(recipient.getUnsubscribeToken())) {
                htmlBody = AdminEmailHtmlEnhancer.withUnsubscribeFooter(
                        htmlBody,
                        recipient.getUnsubscribeToken(),
                        adminEmailProperties.getPublicBaseUrl());
            }
            htmlBody = AdminEmailHtmlEnhancer.withTrackingPixel(
                    htmlBody,
                    recipient.getOpenTrackingToken(),
                    adminEmailProperties.getPublicBaseUrl());

            var listUnsubscribeUrl = email.isIncludeUnsubscribe()
                    ? AdminEmailHtmlEnhancer.unsubscribeUrl(
                            recipient.getUnsubscribeToken(), adminEmailProperties.getPublicBaseUrl())
                    : null;

            try {
                adminHtmlEmailSender.send(
                        mailSender,
                        from,
                        new String[] {recipient.getEmail()},
                        null,
                        null,
                        email.getSubject(),
                        htmlBody,
                        attachments,
                        listUnsubscribeUrl);
                adminEmailStatusResolver.markDelivered(recipient, now);
            } catch (Exception e) {
                log.warn(
                        "Admin email {} failed for {}: {}",
                        sentEmailId,
                        recipient.getEmail(),
                        e.getMessage());
                adminEmailStatusResolver.markFailed(recipient, e.getMessage());
            }
        }

        adminEmailStatusResolver.applyAggregateStatus(email);
        syncOriginalRecipientsAfterResend(email);
        sentEmailRepository.save(email);
    }

    private void syncOriginalRecipientsAfterResend(SentEmail resentEmail) {
        if (resentEmail.getResendOfEmailId() == null) {
            return;
        }

        var original = sentEmailRepository.findById(resentEmail.getResendOfEmailId()).orElse(null);
        if (original == null) {
            return;
        }

        var changed = false;
        for (var resentRecipient : resentEmail.getRecipients()) {
            if (resentRecipient.getStatus() != EmailDeliveryStatus.DELIVERED
                    && resentRecipient.getStatus() != EmailDeliveryStatus.OPENED) {
                continue;
            }

            for (var originalRecipient : original.getRecipients()) {
                if (!adminEmailStatusResolver.matchesEmail(originalRecipient, resentRecipient)) {
                    continue;
                }
                if (originalRecipient.getStatus() != EmailDeliveryStatus.FAILED
                        && originalRecipient.getStatus() != EmailDeliveryStatus.BOUNCED) {
                    continue;
                }

                adminEmailStatusResolver.markDelivered(originalRecipient, resentRecipient.getDeliveredAt());
                changed = true;
            }
        }

        if (changed) {
            adminEmailStatusResolver.applyAggregateStatus(original);
            sentEmailRepository.save(original);
            log.info("Updated original sent email {} after resend {}", original.getId(), resentEmail.getId());
        }
    }

    private void ensureTrackingToken(SentEmailRecipient recipient) {
        if (!StringUtils.hasText(recipient.getOpenTrackingToken())) {
            recipient.setOpenTrackingToken(UUID.randomUUID().toString());
        }
    }

    private void ensureUnsubscribeToken(SentEmailRecipient recipient, boolean includeUnsubscribe) {
        if (includeUnsubscribe && !StringUtils.hasText(recipient.getUnsubscribeToken())) {
            recipient.setUnsubscribeToken(UUID.randomUUID().toString());
        }
    }

    private EmailFromAddress resolveFromAddress(
            SentEmail email, JavaMailSender mailSender, AdminMailConfig mailConfig) {
        if (StringUtils.hasText(email.getFromEmail())) {
            return new EmailFromAddress(email.getFromEmail(), email.getFromName());
        }
        var smtpFrom = adminMailSenderResolver.resolveFrom(mailSender, mailConfig);
        return new EmailFromAddress(smtpFrom, null);
    }

    private AdminHtmlEmailSender.AttachmentPayload toPayload(SentEmailAttachment attachment) {
        return new AdminHtmlEmailSender.AttachmentPayload(
                attachment.getFileName(),
                attachment.getContentType(),
                attachment.getContent());
    }
}
