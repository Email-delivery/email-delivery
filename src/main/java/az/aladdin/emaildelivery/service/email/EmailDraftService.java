package az.aladdin.emaildelivery.service.email;

import az.aladdin.emaildelivery.mapper.EmailDraftMapper;
import az.aladdin.emaildelivery.model.entity.EmailDraft;
import az.aladdin.emaildelivery.model.entity.EmailDraftRecipient;
import az.aladdin.emaildelivery.model.enums.EmailRecipientType;
import az.aladdin.emaildelivery.model.request.email.EmailDraftRecipientRequest;
import az.aladdin.emaildelivery.model.request.email.UpsertEmailDraftRequest;
import az.aladdin.emaildelivery.model.response.email.EmailDraftResponse;
import az.aladdin.emaildelivery.repository.EmailDraftRepository;
import az.aladdin.emaildelivery.util.AuthHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailDraftService {

    private final EmailDraftRepository emailDraftRepository;
    private final EmailDraftMapper emailDraftMapper;
    private final AuthHelper authHelper;

    @Transactional(readOnly = true)
    public Optional<EmailDraftResponse> getCurrent() {
        return emailDraftRepository.findByUserId(currentUserId())
                .map(emailDraftMapper::toResponse);
    }

    @Transactional
    public Optional<EmailDraftResponse> saveCurrent(UpsertEmailDraftRequest request) {
        if (!hasContent(request)) {
            deleteCurrent();
            return Optional.empty();
        }

        var draft = emailDraftRepository.findByUserId(currentUserId()).orElseGet(() -> EmailDraft.builder()
                .userId(currentUserId())
                .build());

        draft.setSubject(trimToNull(request.getSubject()));
        draft.setBodyHtml(trimToNull(request.getBodyHtml()));
        draft.setCampaignId(request.getCampaignId());
        if (request.getShowCc() != null) {
            draft.setShowCc(request.getShowCc());
        }
        if (request.getShowBcc() != null) {
            draft.setShowBcc(request.getShowBcc());
        }

        replaceRecipients(draft, request.getTo(), EmailRecipientType.TO);
        replaceRecipients(draft, request.getCc(), EmailRecipientType.CC);
        replaceRecipients(draft, request.getBcc(), EmailRecipientType.BCC);

        var saved = emailDraftRepository.save(draft);
        log.debug("Saved email draft {} for user {}", saved.getId(), saved.getUserId());
        return Optional.of(emailDraftMapper.toResponse(saved));
    }

    @Transactional
    public void deleteCurrent() {
        emailDraftRepository.findByUserId(currentUserId()).ifPresent(draft -> {
            emailDraftRepository.delete(draft);
            log.debug("Deleted email draft {} for user {}", draft.getId(), draft.getUserId());
        });
    }

    @Transactional
    public void deleteForUser(Long userId) {
        emailDraftRepository.findByUserId(userId).ifPresent(emailDraftRepository::delete);
    }

    private void replaceRecipients(
            EmailDraft draft,
            List<EmailDraftRecipientRequest> contacts,
            EmailRecipientType type) {
        draft.getRecipients().removeIf(recipient -> recipient.getRecipientType() == type);
        if (contacts == null || contacts.isEmpty()) {
            return;
        }

        var unique = new LinkedHashMap<String, EmailDraftRecipientRequest>();
        for (var contact : contacts) {
            if (contact == null || !StringUtils.hasText(contact.getEmail())) {
                continue;
            }
            unique.putIfAbsent(normalizeEmail(contact.getEmail()), contact);
        }

        for (var contact : unique.values()) {
            draft.getRecipients().add(EmailDraftRecipient.builder()
                    .draft(draft)
                    .email(normalizeEmail(contact.getEmail()))
                    .name(trimToNull(contact.getName()))
                    .recipientType(type)
                    .build());
        }
    }

    private boolean hasContent(UpsertEmailDraftRequest request) {
        if (StringUtils.hasText(request.getSubject()) || StringUtils.hasText(request.getBodyHtml())) {
            return true;
        }
        return hasRecipients(request.getTo()) || hasRecipients(request.getCc()) || hasRecipients(request.getBcc());
    }

    private boolean hasRecipients(List<EmailDraftRecipientRequest> recipients) {
        return recipients != null
                && recipients.stream().anyMatch(r -> r != null && StringUtils.hasText(r.getEmail()));
    }

    private Long currentUserId() {
        return authHelper.getAuthenticatedUser().getUserId();
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
