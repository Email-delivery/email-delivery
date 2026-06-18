package az.aladdin.emaildelivery.service.email;

import az.aladdin.emaildelivery.config.AdminEmailProperties;
import az.aladdin.emaildelivery.exception.BadException;
import az.aladdin.emaildelivery.exception.EntityNames;
import az.aladdin.emaildelivery.exception.MessageKeys;
import az.aladdin.emaildelivery.exception.NotFoundException;
import az.aladdin.emaildelivery.mapper.SentEmailMapper;
import az.aladdin.emaildelivery.model.entity.EmailCampaign;
import az.aladdin.emaildelivery.model.entity.SentEmail;
import az.aladdin.emaildelivery.model.entity.SentEmailAttachment;
import az.aladdin.emaildelivery.model.entity.SentEmailRecipient;
import az.aladdin.emaildelivery.model.enums.EmailDeliveryStatus;
import az.aladdin.emaildelivery.model.enums.EmailRecipientType;
import az.aladdin.emaildelivery.model.enums.EmailResendMode;
import az.aladdin.emaildelivery.model.request.email.EmailAttachmentRequest;
import az.aladdin.emaildelivery.model.request.email.ResendEmailRequest;
import az.aladdin.emaildelivery.model.request.email.SendAdminEmailRequest;
import az.aladdin.emaildelivery.model.request.email.SendCampaignRequest;
import az.aladdin.emaildelivery.model.request.email.UpdateEmailScheduleRequest;
import az.aladdin.emaildelivery.model.response.email.SendEmailResponse;
import az.aladdin.emaildelivery.model.response.email.SentEmailResponse;
import az.aladdin.emaildelivery.repository.SentEmailRepository;
import az.aladdin.emaildelivery.util.AuthHelper;
import az.aladdin.emaildelivery.util.InstantFormatting;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminEmailService {

    private final SentEmailRepository sentEmailRepository;
    private final SentEmailMapper sentEmailMapper;
    private final AdminEmailDispatchProcessor adminEmailDispatchProcessor;
    private final AdminEmailProperties adminEmailProperties;
    private final EmailCampaignService emailCampaignService;
    private final EmailTemplateService emailTemplateService;
    private final EmailDraftService emailDraftService;
    private final AdminEmailUnsubscribeService adminEmailUnsubscribeService;
    private final EmailSenderIdentityService emailSenderIdentityService;
    private final AuthHelper authHelper;

    @Transactional
    public SendEmailResponse send(SendAdminEmailRequest request) {
        validateRequest(request);

        var principal = authHelper.getAuthenticatedUser();
        var now = Instant.now();
        var dedupedTo = filterSuppressed(deduplicateEmails(request.getTo()));
        var dedupedCc = filterSuppressed(deduplicateEmails(request.getCc()));
        var dedupedBcc = filterSuppressed(deduplicateEmails(request.getBcc()));
        var suppressedCount = countSuppressed(request.getTo(), request.getCc(), request.getBcc(), dedupedTo, dedupedCc, dedupedBcc);

        if (dedupedTo.isEmpty()) {
            throw new BadException(EntityNames.SENT_EMAIL, MessageKeys.EXCEPTION_EMAIL_ALL_RECIPIENTS_SUPPRESSED);
        }

        var includeUnsubscribe = Boolean.TRUE.equals(request.getIncludeUnsubscribe())
                || (request.getIncludeUnsubscribe() == null && request.getCampaignId() != null);

        if (request.getCampaignId() != null) {
            emailCampaignService.findById(request.getCampaignId());
        }

        var scheduledAt = request.getScheduledAt();
        var isScheduled = scheduledAt != null && scheduledAt.isAfter(now);
        if (scheduledAt != null && !isScheduled) {
            throw new BadException(EntityNames.SENT_EMAIL, MessageKeys.EXCEPTION_EMAIL_SCHEDULE_IN_PAST);
        }

        var fromAddress = emailSenderIdentityService.resolveForSend(request.getSenderIdentityId());

        var sentEmail = SentEmail.builder()
                .subject(request.getSubject().trim())
                .bodyHtml(request.getBodyHtml().trim())
                .sentByUserId(principal.getUserId())
                .sentByEmail(principal.getEmail())
                .sentByName(principal.getFirstName() + " " + principal.getLastName())
                .senderIdentityId(request.getSenderIdentityId())
                .fromEmail(fromAddress.email())
                .fromName(fromAddress.displayName())
                .sentAt(isScheduled ? scheduledAt : now)
                .scheduledAt(isScheduled ? scheduledAt : null)
                .status(isScheduled ? EmailDeliveryStatus.SCHEDULED : EmailDeliveryStatus.QUEUED)
                .campaignId(request.getCampaignId())
                .resendOfEmailId(request.getResendOfEmailId())
                .includeUnsubscribe(includeUnsubscribe)
                .build();

        addRecipients(sentEmail, dedupedTo, EmailRecipientType.TO, includeUnsubscribe);
        addRecipients(sentEmail, dedupedCc, EmailRecipientType.CC, includeUnsubscribe);
        addRecipients(sentEmail, dedupedBcc, EmailRecipientType.BCC, includeUnsubscribe);
        addAttachments(sentEmail, request.getAttachments());

        var saved = sentEmailRepository.save(sentEmail);
        if (!isScheduled) {
            scheduleDispatchAfterCommit(saved.getId());
        }
        emailDraftService.deleteForUser(principal.getUserId());

        var recipientCount = dedupedTo.size() + dedupedCc.size() + dedupedBcc.size();
        if (isScheduled) {
            log.info("Scheduled admin email {} to {} recipient(s) at {}", saved.getId(), recipientCount, scheduledAt);
        } else {
            log.info("Queued admin email {} to {} recipient(s)", saved.getId(), recipientCount);
        }

        return SendEmailResponse.builder()
                .id(String.valueOf(saved.getId()))
                .sentAt(InstantFormatting.toApiString(saved.getSentAt()))
                .scheduledAt(isScheduled ? InstantFormatting.toApiString(scheduledAt) : null)
                .status(saved.getStatus().name().toLowerCase(Locale.ROOT))
                .recipientCount(recipientCount)
                .suppressedCount(suppressedCount)
                .build();
    }

    @Transactional(readOnly = true)
    public List<SentEmailResponse> history() {
        return sentEmailRepository.findAllWithRecipients().stream()
                .map(sentEmailMapper::toResponse)
                .toList();
    }

    @Transactional
    public SentEmailResponse updateSchedule(Long id, UpdateEmailScheduleRequest request) {
        var email = sentEmailRepository.findByIdWithRecipients(id)
                .orElseThrow(() -> new NotFoundException(
                        EntityNames.SENT_EMAIL,
                        MessageKeys.EXCEPTION_NOT_FOUND,
                        id));

        if (email.getStatus() != EmailDeliveryStatus.SCHEDULED) {
            throw new BadException(EntityNames.SENT_EMAIL, MessageKeys.EXCEPTION_EMAIL_NOT_SCHEDULED);
        }

        var scheduledAt = request.getScheduledAt();
        var now = Instant.now();
        if (scheduledAt == null || !scheduledAt.isAfter(now)) {
            throw new BadException(EntityNames.SENT_EMAIL, MessageKeys.EXCEPTION_EMAIL_SCHEDULE_IN_PAST);
        }

        email.setScheduledAt(scheduledAt);
        email.setSentAt(scheduledAt);
        var saved = sentEmailRepository.save(email);
        log.info("Rescheduled email {} to {}", id, scheduledAt);
        return sentEmailMapper.toResponse(saved);
    }

    @Transactional
    public SendEmailResponse resend(Long id, ResendEmailRequest request) {
        var original = sentEmailRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        EntityNames.SENT_EMAIL,
                        MessageKeys.EXCEPTION_NOT_FOUND,
                        id));

        original.getRecipients().size();
        original.getAttachments().size();

        var mode = request != null && request.getMode() != null ? request.getMode() : EmailResendMode.ALL;
        var sourceRecipients = original.getRecipients().stream()
                .filter(recipient -> matchesResendMode(recipient, mode))
                .toList();

        if (sourceRecipients.isEmpty()) {
            throw new BadException(EntityNames.SENT_EMAIL, MessageKeys.EXCEPTION_EMAIL_RESEND_NO_RECIPIENTS);
        }

        var to = sourceRecipients.stream()
                .filter(r -> r.getRecipientType() == EmailRecipientType.TO)
                .map(SentEmailRecipient::getEmail)
                .toList();
        var cc = sourceRecipients.stream()
                .filter(r -> r.getRecipientType() == EmailRecipientType.CC)
                .map(SentEmailRecipient::getEmail)
                .toList();
        var bcc = sourceRecipients.stream()
                .filter(r -> r.getRecipientType() == EmailRecipientType.BCC)
                .map(SentEmailRecipient::getEmail)
                .toList();

        if (to.isEmpty()) {
            throw new BadException(EntityNames.SENT_EMAIL, MessageKeys.EXCEPTION_EMAIL_RESEND_NO_RECIPIENTS);
        }

        var attachmentRequests = original.getAttachments().stream()
                .map(attachment -> EmailAttachmentRequest.builder()
                        .fileName(attachment.getFileName())
                        .contentType(attachment.getContentType())
                        .contentBase64(Base64.getEncoder().encodeToString(attachment.getContent()))
                        .build())
                .toList();

        return send(SendAdminEmailRequest.builder()
                .to(to)
                .cc(cc.isEmpty() ? null : cc)
                .bcc(bcc.isEmpty() ? null : bcc)
                .subject(original.getSubject())
                .bodyHtml(original.getBodyHtml())
                .campaignId(original.getCampaignId())
                .resendOfEmailId(original.getId())
                .includeUnsubscribe(original.isIncludeUnsubscribe())
                .senderIdentityId(original.getSenderIdentityId())
                .attachments(attachmentRequests)
                .build());
    }

    @Transactional
    public SendEmailResponse sendCampaign(Long campaignId, SendCampaignRequest request) {
        var campaign = emailCampaignService.findById(campaignId);
        campaign.getContacts().size();

        if (campaign.getContacts().isEmpty()) {
            throw new BadException(EntityNames.EMAIL_CAMPAIGN, MessageKeys.EXCEPTION_CAMPAIGN_NO_CONTACTS);
        }

        var overrides = request != null ? request : SendCampaignRequest.builder().build();
        var subject = resolveCampaignSubject(campaign, overrides.getSubject());
        var bodyHtml = resolveCampaignBodyHtml(campaign, overrides.getBodyHtml());

        if (!StringUtils.hasText(subject)) {
            throw new BadException(EntityNames.EMAIL_CAMPAIGN, MessageKeys.EXCEPTION_CAMPAIGN_MISSING_SUBJECT);
        }
        if (!StringUtils.hasText(bodyHtml)) {
            throw new BadException(EntityNames.EMAIL_CAMPAIGN, MessageKeys.EXCEPTION_CAMPAIGN_MISSING_BODY);
        }

        var to = campaign.getContacts().stream()
                .map(contact -> contact.getEmail())
                .toList();

        return send(SendAdminEmailRequest.builder()
                .to(to)
                .cc(overrides.getCc())
                .bcc(overrides.getBcc())
                .subject(subject.trim())
                .bodyHtml(bodyHtml.trim())
                .campaignId(campaignId)
                .attachments(overrides.getAttachments())
                .scheduledAt(overrides.getScheduledAt())
                .senderIdentityId(overrides.getSenderIdentityId())
                .includeUnsubscribe(
                        overrides.getIncludeUnsubscribe() != null ? overrides.getIncludeUnsubscribe() : true)
                .build());
    }

    private String resolveCampaignSubject(EmailCampaign campaign, String override) {
        if (StringUtils.hasText(override)) {
            return override.trim();
        }
        if (StringUtils.hasText(campaign.getDefaultSubject())) {
            return campaign.getDefaultSubject();
        }
        if (campaign.getTemplateId() != null) {
            return emailTemplateService.findById(campaign.getTemplateId()).getSubject();
        }
        return null;
    }

    private String resolveCampaignBodyHtml(EmailCampaign campaign, String override) {
        if (StringUtils.hasText(override)) {
            return override.trim();
        }
        if (StringUtils.hasText(campaign.getDefaultHtmlBody())) {
            return campaign.getDefaultHtmlBody();
        }
        if (campaign.getTemplateId() != null) {
            return emailTemplateService.findById(campaign.getTemplateId()).getHtmlBody();
        }
        return null;
    }

    private void scheduleDispatchAfterCommit(Long sentEmailId) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    adminEmailDispatchProcessor.dispatch(sentEmailId);
                }
            });
            return;
        }
        adminEmailDispatchProcessor.dispatch(sentEmailId);
    }

    private boolean matchesResendMode(SentEmailRecipient recipient, EmailResendMode mode) {
        if (mode == EmailResendMode.ALL) {
            return true;
        }
        var status = recipient.getStatus();
        return status == EmailDeliveryStatus.FAILED || status == EmailDeliveryStatus.BOUNCED;
    }

    private void validateRequest(SendAdminEmailRequest request) {
        var totalRecipients = request.getTo().size()
                + (request.getCc() != null ? request.getCc().size() : 0)
                + (request.getBcc() != null ? request.getBcc().size() : 0);

        if (totalRecipients > adminEmailProperties.getMaxRecipientsPerRequest()) {
            throw new BadException(
                    EntityNames.SENT_EMAIL,
                    MessageKeys.EXCEPTION_EMAIL_RECIPIENT_LIMIT,
                    adminEmailProperties.getMaxRecipientsPerRequest());
        }

        if (request.getSubject().length() > adminEmailProperties.getMaxSubjectLength()) {
            throw new BadException(
                    EntityNames.SENT_EMAIL,
                    MessageKeys.EXCEPTION_EMAIL_SUBJECT_TOO_LONG,
                    adminEmailProperties.getMaxSubjectLength());
        }

        if (request.getBodyHtml().length() > adminEmailProperties.getMaxHtmlBodyLength()) {
            throw new BadException(
                    EntityNames.SENT_EMAIL,
                    MessageKeys.EXCEPTION_EMAIL_BODY_TOO_LONG,
                    adminEmailProperties.getMaxHtmlBodyLength());
        }

        var attachments = request.getAttachments();
        if (attachments != null && attachments.size() > adminEmailProperties.getMaxAttachmentsPerEmail()) {
            throw new BadException(
                    EntityNames.SENT_EMAIL,
                    MessageKeys.EXCEPTION_EMAIL_ATTACHMENT_LIMIT,
                    adminEmailProperties.getMaxAttachmentsPerEmail());
        }
    }

    private List<String> deduplicateEmails(List<String> emails) {
        if (emails == null || emails.isEmpty()) {
            return List.of();
        }
        Set<String> unique = new LinkedHashSet<>();
        for (var email : emails) {
            unique.add(email.trim().toLowerCase(Locale.ROOT));
        }
        return new ArrayList<>(unique);
    }

    private List<String> filterSuppressed(List<String> emails) {
        return adminEmailUnsubscribeService.filterSuppressed(emails);
    }

    private int countSuppressed(
            List<String> originalTo,
            List<String> originalCc,
            List<String> originalBcc,
            List<String> filteredTo,
            List<String> filteredCc,
            List<String> filteredBcc) {
        var before = deduplicateEmails(originalTo).size()
                + deduplicateEmails(originalCc).size()
                + deduplicateEmails(originalBcc).size();
        var after = filteredTo.size() + filteredCc.size() + filteredBcc.size();
        return Math.max(0, before - after);
    }

    private void addRecipients(
            SentEmail sentEmail,
            List<String> emails,
            EmailRecipientType type,
            boolean includeUnsubscribe) {
        for (var email : emails) {
            var builder = SentEmailRecipient.builder()
                    .sentEmail(sentEmail)
                    .email(email)
                    .recipientType(type)
                    .status(EmailDeliveryStatus.QUEUED)
                    .openTrackingToken(UUID.randomUUID().toString());
            if (includeUnsubscribe) {
                builder.unsubscribeToken(UUID.randomUUID().toString());
            }
            sentEmail.getRecipients().add(builder.build());
        }
    }

    private void addAttachments(SentEmail sentEmail, List<EmailAttachmentRequest> attachments) {
        if (attachments == null || attachments.isEmpty()) {
            return;
        }

        for (var attachment : attachments) {
            byte[] content;
            try {
                content = Base64.getDecoder().decode(attachment.getContentBase64());
            } catch (IllegalArgumentException e) {
                throw new BadException(EntityNames.SENT_EMAIL, MessageKeys.EXCEPTION_EMAIL_ATTACHMENT_INVALID);
            }

            if (content.length > adminEmailProperties.getMaxAttachmentBytes()) {
                throw new BadException(
                        EntityNames.SENT_EMAIL,
                        MessageKeys.EXCEPTION_EMAIL_ATTACHMENT_TOO_LARGE,
                        adminEmailProperties.getMaxAttachmentBytes());
            }

            sentEmail.getAttachments().add(SentEmailAttachment.builder()
                    .sentEmail(sentEmail)
                    .fileName(attachment.getFileName().trim())
                    .contentType(attachment.getContentType().trim())
                    .content(content)
                    .build());
        }
    }
}
