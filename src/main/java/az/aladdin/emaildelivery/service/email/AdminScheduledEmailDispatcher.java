package az.aladdin.emaildelivery.service.email;

import az.aladdin.emaildelivery.model.enums.EmailDeliveryStatus;
import az.aladdin.emaildelivery.repository.SentEmailRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminScheduledEmailDispatcher {

    private final SentEmailRepository sentEmailRepository;
    private final AdminEmailDispatchProcessor adminEmailDispatchProcessor;

    @Scheduled(fixedDelay = 30_000)
    @Transactional
    public void dispatchDueScheduledEmails() {
        var dueEmails = sentEmailRepository.findByStatusAndScheduledAtLessThanEqual(
                EmailDeliveryStatus.SCHEDULED, Instant.now());
        for (var email : dueEmails) {
            email.setStatus(EmailDeliveryStatus.QUEUED);
            email.setSentAt(Instant.now());
            email.setScheduledAt(null);
            sentEmailRepository.save(email);
            adminEmailDispatchProcessor.dispatch(email.getId());
            log.info("Dispatched scheduled email {}", email.getId());
        }
    }
}
