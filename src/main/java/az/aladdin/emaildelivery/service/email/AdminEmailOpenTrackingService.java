package az.aladdin.emaildelivery.service.email;

import az.aladdin.emaildelivery.model.enums.EmailDeliveryStatus;
import az.aladdin.emaildelivery.repository.SentEmailRecipientRepository;
import az.aladdin.emaildelivery.repository.SentEmailRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminEmailOpenTrackingService {

    private final SentEmailRecipientRepository sentEmailRecipientRepository;
    private final SentEmailRepository sentEmailRepository;
    private final AdminEmailStatusResolver adminEmailStatusResolver;

    @Transactional
    public void recordOpen(String token) {
        var recipient = sentEmailRecipientRepository.findByOpenTrackingToken(token).orElse(null);
        if (recipient == null) {
            return;
        }

        var email = recipient.getSentEmail();
        var now = Instant.now();

        if (recipient.getStatus() != EmailDeliveryStatus.OPENED) {
            adminEmailStatusResolver.markOpened(recipient, now);
            adminEmailStatusResolver.applyAggregateStatus(email);
            sentEmailRepository.save(email);
            log.info("Email open recorded for {} on sent email {}", recipient.getEmail(), email.getId());
        }
    }
}
