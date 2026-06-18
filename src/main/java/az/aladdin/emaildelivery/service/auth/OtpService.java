package az.aladdin.emaildelivery.service.auth;

import az.aladdin.emaildelivery.exception.EntityNames;
import az.aladdin.emaildelivery.exception.MessageKeys;
import az.aladdin.emaildelivery.exception.NotFoundException;
import az.aladdin.emaildelivery.model.entity.OtpEntity;
import az.aladdin.emaildelivery.model.enums.OtpPurpose;
import az.aladdin.emaildelivery.repository.OtpRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpService {

    private static final int OTP_EXPIRATION_MINUTES = 5;
    private static final int MAX_RETRY_COUNT = 5;

    private final OtpRepository otpRepository;
    private final AdminOtpEmailService emailService;

    @Transactional
    public void sendOtp(String email, String recipientName, OtpPurpose purpose) {
        LocalDateTime now = LocalDateTime.now();
        String normalizedEmail = normalizeEmail(email);
        Optional<OtpEntity> existingOtp = otpRepository
                .findFirstByEmailAndPurposeAndUsedFalseOrderByCreatedAtDesc(normalizedEmail, purpose);

        if (existingOtp.isPresent()) {
            OtpEntity otpEntity = existingOtp.get();
            if (otpEntity.getExpirationTime().isAfter(now) && otpEntity.getRetryCount() < MAX_RETRY_COUNT) {
                otpEntity.setRetryCount(otpEntity.getRetryCount() + 1);
                otpEntity.setLastSentTime(now);
                otpRepository.save(otpEntity);
                emailService.sendOtp(normalizedEmail, recipientName, String.valueOf(otpEntity.getCode()), purpose);
                log.info("OTP resent to {} for purpose {}", normalizedEmail, purpose);
                return;
            }
            otpEntity.setUsed(true);
            otpRepository.save(otpEntity);
        }

        String code = generateAndSaveOtp(normalizedEmail, purpose);
        emailService.sendOtp(normalizedEmail, recipientName, code, purpose);
        log.info("New OTP sent to {} for purpose {}", normalizedEmail, purpose);
    }

    @Transactional
    public void verifyOtp(String email, Integer code, OtpPurpose purpose) {
        String normalizedEmail = normalizeEmail(email);
        OtpEntity otpEntity = otpRepository
                .findByCodeAndEmailAndPurposeAndUsedFalse(code, normalizedEmail, purpose)
                .orElseThrow(() -> new NotFoundException(EntityNames.NOT_FOUND_RESOURCE,
                        MessageKeys.EXCEPTION_NOT_FOUND, MessageKeys.ENTITY_OTP));

        if (otpEntity.getExpirationTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException(MessageKeys.EXCEPTION_OTP_EXPIRED);
        }

        otpEntity.setUsed(true);
        otpRepository.save(otpEntity);
        log.info("OTP verified for {} with purpose {}", normalizedEmail, purpose);
    }

    public String generateAndSaveOtp(String email, OtpPurpose purpose) {
        SecureRandom random = new SecureRandom();
        int code = 100_000 + random.nextInt(900_000);
        LocalDateTime now = LocalDateTime.now();
        OtpEntity otpEntity = OtpEntity.builder()
                .email(normalizeEmail(email))
                .code(code)
                .purpose(purpose)
                .expirationTime(now.plusMinutes(OTP_EXPIRATION_MINUTES))
                .retryCount(1)
                .lastSentTime(now)
                .used(false)
                .build();
        otpRepository.save(otpEntity);
        return String.valueOf(code);
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }
}
