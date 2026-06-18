package az.aladdin.emaildelivery.service.email;

import az.aladdin.emaildelivery.model.entity.EmailSuppression;
import az.aladdin.emaildelivery.model.entity.SentEmailRecipient;
import az.aladdin.emaildelivery.model.enums.EmailSuppressionSource;
import az.aladdin.emaildelivery.repository.EmailSuppressionRepository;
import az.aladdin.emaildelivery.repository.SentEmailRecipientRepository;
import az.aladdin.emaildelivery.repository.SentEmailRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminEmailUnsubscribeService {

    private final SentEmailRecipientRepository sentEmailRecipientRepository;
    private final SentEmailRepository sentEmailRepository;
    private final EmailSuppressionRepository emailSuppressionRepository;

    @Transactional
    public boolean unsubscribe(String token) {
        var recipient = sentEmailRecipientRepository.findByUnsubscribeToken(token).orElse(null);
        if (recipient == null) {
            return false;
        }

        suppressRecipient(recipient, EmailSuppressionSource.ONE_CLICK);
        return true;
    }

    @Transactional
    public void suppressRecipient(SentEmailRecipient recipient, EmailSuppressionSource source) {
        var normalized = normalizeEmail(recipient.getEmail());
        var now = Instant.now();

        emailSuppressionRepository.findExistingEmailsLowercase(Set.of(normalized)).stream()
                .findFirst()
                .ifPresentOrElse(
                        ignored -> { },
                        () -> emailSuppressionRepository.save(EmailSuppression.builder()
                                .email(normalized)
                                .unsubscribedAt(now)
                                .source(source)
                                .campaignId(recipient.getSentEmail().getCampaignId())
                                .sentEmailId(recipient.getSentEmail().getId())
                                .build()));

        if (recipient.getUnsubscribedAt() == null) {
            recipient.setUnsubscribedAt(now);
            sentEmailRecipientRepository.save(recipient);
            log.info("Email {} unsubscribed via {}", normalized, source);
        }
    }

    @Transactional(readOnly = true)
    public boolean isSuppressed(String email) {
        return emailSuppressionRepository.existsByEmailIgnoreCase(normalizeEmail(email));
    }

    @Transactional(readOnly = true)
    public Set<String> findSuppressedEmails(Collection<String> emails) {
        if (emails == null || emails.isEmpty()) {
            return Set.of();
        }
        var normalized = emails.stream()
                .filter(e -> e != null && !e.isBlank())
                .map(this::normalizeEmail)
                .toList();
        if (normalized.isEmpty()) {
            return Set.of();
        }
        return emailSuppressionRepository.findExistingEmailsLowercase(normalized);
    }

    @Transactional(readOnly = true)
    public List<String> filterSuppressed(List<String> emails) {
        if (emails == null || emails.isEmpty()) {
            return List.of();
        }
        var suppressed = findSuppressedEmails(emails);
        var result = new ArrayList<String>();
        var seen = new LinkedHashSet<String>();
        for (var email : emails) {
            var normalized = normalizeEmail(email);
            if (suppressed.contains(normalized) || !seen.add(normalized)) {
                continue;
            }
            result.add(normalized);
        }
        return result;
    }

    @Transactional
    public void resubscribe(Long suppressionId) {
        emailSuppressionRepository.deleteById(suppressionId);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
