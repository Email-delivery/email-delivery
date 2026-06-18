package az.aladdin.emaildelivery.service.email;

import az.aladdin.emaildelivery.email.EmailFromAddress;
import az.aladdin.emaildelivery.exception.BadException;
import az.aladdin.emaildelivery.exception.EntityNames;
import az.aladdin.emaildelivery.exception.MessageKeys;
import az.aladdin.emaildelivery.exception.NotFoundException;
import az.aladdin.emaildelivery.model.entity.EmailSenderIdentity;
import az.aladdin.emaildelivery.model.request.email.UpsertEmailSenderIdentityRequest;
import az.aladdin.emaildelivery.model.response.email.EmailSenderIdentityResponse;
import az.aladdin.emaildelivery.repository.EmailSenderIdentityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmailSenderIdentityService {

    private final EmailSenderIdentityRepository emailSenderIdentityRepository;
    private final AdminMailConfigService adminMailConfigService;

    @Transactional(readOnly = true)
    public List<EmailSenderIdentityResponse> listActive() {
        return emailSenderIdentityRepository.findByActiveTrueOrderByDisplayNameAsc().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EmailSenderIdentityResponse> listAll() {
        return emailSenderIdentityRepository.findAll().stream()
                .sorted((a, b) -> a.getDisplayName().compareToIgnoreCase(b.getDisplayName()))
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public EmailSenderIdentity findActiveById(Long id) {
        return emailSenderIdentityRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new NotFoundException(
                        EntityNames.EMAIL_SENDER_IDENTITY,
                        MessageKeys.EXCEPTION_NOT_FOUND,
                        id));
    }

    @Transactional
    public EmailSenderIdentityResponse create(UpsertEmailSenderIdentityRequest request) {
        var email = normalizeEmail(request.getEmail());
        if (emailSenderIdentityRepository.existsByEmailIgnoreCase(email)) {
            throw new BadException(EntityNames.EMAIL_SENDER_IDENTITY, MessageKeys.EXCEPTION_EMAIL_SENDER_EXISTS);
        }

        var identity = EmailSenderIdentity.builder()
                .email(email)
                .displayName(request.getDisplayName().trim())
                .defaultSender(Boolean.TRUE.equals(request.getDefaultSender()))
                .active(request.getActive() == null || request.getActive())
                .build();

        if (identity.isDefaultSender()) {
            emailSenderIdentityRepository.clearDefaultSender();
        }

        return toResponse(emailSenderIdentityRepository.save(identity));
    }

    @Transactional
    public EmailSenderIdentityResponse update(Long id, UpsertEmailSenderIdentityRequest request) {
        var identity = emailSenderIdentityRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        EntityNames.EMAIL_SENDER_IDENTITY,
                        MessageKeys.EXCEPTION_NOT_FOUND,
                        id));

        var email = normalizeEmail(request.getEmail());
        if (emailSenderIdentityRepository.existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw new BadException(EntityNames.EMAIL_SENDER_IDENTITY, MessageKeys.EXCEPTION_EMAIL_SENDER_EXISTS);
        }

        identity.setEmail(email);
        identity.setDisplayName(request.getDisplayName().trim());
        if (request.getActive() != null) {
            identity.setActive(request.getActive());
        }

        if (Boolean.TRUE.equals(request.getDefaultSender())) {
            emailSenderIdentityRepository.clearDefaultSender();
            identity.setDefaultSender(true);
        } else if (request.getDefaultSender() != null) {
            identity.setDefaultSender(false);
        }

        return toResponse(emailSenderIdentityRepository.save(identity));
    }

    @Transactional
    public void delete(Long id) {
        var identity = emailSenderIdentityRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        EntityNames.EMAIL_SENDER_IDENTITY,
                        MessageKeys.EXCEPTION_NOT_FOUND,
                        id));
        emailSenderIdentityRepository.delete(identity);
    }

    @Transactional(readOnly = true)
    public EmailFromAddress resolveForSend(Long senderIdentityId) {
        if (senderIdentityId != null) {
            var identity = findActiveById(senderIdentityId);
            return toFromAddress(identity);
        }
        return resolveDefault();
    }

    @Transactional(readOnly = true)
    public EmailFromAddress resolveDefault() {
        return emailSenderIdentityRepository.findByDefaultSenderTrueAndActiveTrue()
                .map(this::toFromAddress)
                .orElseGet(this::fallbackFromSmtp);
    }

    private EmailFromAddress fallbackFromSmtp() {
        var config = adminMailConfigService.findActiveConfig();
        if (config != null && StringUtils.hasText(config.getUsername())) {
            return new EmailFromAddress(config.getUsername().trim(), null);
        }
        return new EmailFromAddress("noreply@localhost", null);
    }

    private EmailFromAddress toFromAddress(EmailSenderIdentity identity) {
        return new EmailFromAddress(identity.getEmail(), identity.getDisplayName());
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    public static String formatLabel(String displayName, String email) {
        return EmailFromAddress.formatLabel(displayName, email);
    }

    private EmailSenderIdentityResponse toResponse(EmailSenderIdentity identity) {
        return EmailSenderIdentityResponse.builder()
                .id(String.valueOf(identity.getId()))
                .email(identity.getEmail())
                .displayName(identity.getDisplayName())
                .defaultSender(identity.isDefaultSender())
                .active(identity.isActive())
                .label(EmailFromAddress.formatLabel(identity.getDisplayName(), identity.getEmail()))
                .build();
    }
}
